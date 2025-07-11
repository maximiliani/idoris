/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.idoris.rules.logic;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Central service that manages rule discovery and execution based on precomputed dependency graphs.
 * <p>
 * The RuleService is the orchestration core of the rule execution engine. It leverages a
 * precomputed dependency graph generated at compile-time by the annotation processor to
 * efficiently execute rules in the correct order. This approach eliminates the overhead
 * of runtime dependency resolution and enables optimal parallel execution.
 * <p>
 * This service follows an optimization-first design with several key principles:
 * <ul>
 *   <li><strong>Just-in-time rule instantiation</strong>: Only rules referenced in the precomputed
 *       graph are loaded, reducing memory usage and startup time</li>
 *   <li><strong>Zero runtime dependency calculation</strong>: All rule ordering is determined
 *       at compile-time through static analysis</li>
 *   <li><strong>Maximum parallelism</strong>: Rules are executed concurrently using CompletableFuture
 *       while still respecting their execution order</li>
 *   <li><strong>Type-safe execution</strong>: Strong generic typing ensures rules receive
 *       compatible input types and produce correct output types</li>
 *   <li><strong>Resilient processing</strong>: Failures in individual rules are isolated and won't
 *       cause the entire rule processing pipeline to fail</li>
 * </ul>
 * <p>
 * <strong>Usage example:</strong>
 * <pre>
 * {@code
 * // Create a rule result factory
 * Supplier<ValidationResult> resultFactory = ValidationResult::new;
 *
 * // Execute validation rules for an Operation
 * ValidationResult result = ruleService.executeRules(
 *     RuleTask.VALIDATE,
 *     operation,
 *     resultFactory
 * );
 * }
 * </pre>
 * <p>
 * <strong>Extension points:</strong> The rule engine can be extended by implementing the {@link IRule}
 * interface and annotating the implementation with {@link Rule}. The annotation processor will
 * automatically incorporate the new rule into the precomputed graph.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleService {

    /**
     * Provides access to all Spring-managed beans for rule discovery.
     * <p>
     * Used to selectively load only the rule implementations that are actually referenced
     * in the precomputed dependency graph, avoiding the instantiation of unused rules.
     */
    private final ListableBeanFactory beanFactory;

    /**
     * Thread-safe registry mapping fully qualified class names to rule instances.
     * <p>
     * This registry serves as a cache of rule implementations that have been loaded from the
     * Spring context. It uses the fully qualified class name as the key since that's what
     * the precomputed graph uses to identify rules. The {@link ConcurrentHashMap} implementation
     * ensures thread safety during concurrent rule execution.
     */
    private final Map<String, IRule<?, ?>> ruleRegistry = new ConcurrentHashMap<>();

    /**
     * The precomputed rule dependency graph loaded from generated code.
     * <p>
     * This graph contains the topologically sorted rule class names for each combination of
     * rule task and target element type. It's generated at compile-time by the annotation
     * processor, eliminating the need for runtime dependency analysis. The graph is the
     * authoritative source for rule execution order.
     */
    private PrecomputedRuleGraph precomputedGraph;

    /**
     * Initializes the rule engine by loading the precomputed dependency graph and
     * discovering required rule implementations.
     * <p>
     * This method follows a two-step initialization process:
     * <ol>
     *   <li>First, it loads the precomputed rule dependency graph from the generated implementation</li>
     *   <li>Then, it selectively loads only the rule implementations that are referenced in the graph</li>
     * </ol>
     * <p>
     * This approach ensures that only rules that are actually needed are instantiated, reducing
     * memory usage and startup time. It also allows the rule engine to provide detailed diagnostic
     * information about missing rules at startup rather than failing at runtime.
     * <p>
     * This method is automatically called by Spring after dependency injection is complete.
     *
     * @throws IllegalStateException if the rule engine initialization fails, either because
     *                               the precomputed graph couldn't be loaded or because critical
     *                               rule implementations are missing
     */
    @PostConstruct
    void initialize() {
        try {
            // Load precomputed graph first to know which rules we need
            loadPrecomputedGraph();

            // Only discover rules that are actually referenced in the precomputed graph
            discoverRequiredRules();

            log.info("Rule engine initialized with {} rules", ruleRegistry.size());
        } catch (Exception e) {
            log.error("Failed to initialize rule engine", e);
            throw new IllegalStateException("Rule engine initialization failed", e);
        }
    }

    /**
     * Loads the precomputed rule dependency graph from generated code.
     * <p>
     * This method uses reflection to instantiate the implementation class of {@link PrecomputedRuleGraph}
     * that was generated by the annotation processor at compile time. The generated class contains
     * the complete dependency information for all rules, including their topological sorting.
     * <p>
     * The precomputed graph is the source of truth for rule execution order and is used to:
     * <ul>
     *   <li>Determine which rule implementations need to be loaded from Spring</li>
     *   <li>Determine the correct execution order of rules for each task and element type</li>
     *   <li>Optimize rule execution by enabling parallel processing where possible</li>
     * </ul>
     *
     * @throws IllegalStateException if the precomputed graph implementation cannot be loaded,
     *                               which could happen if the annotation processor didn't run
     *                               or if the generated class is not on the classpath
     */
    private void loadPrecomputedGraph() {
        log.info("Loading precomputed rule dependency graph...");

        try {
            // Load the generated implementation class
            Class<?> implClass = Class.forName("edu.kit.datamanager.idoris.rules.logic.PrecomputedRuleGraphImpl");
            precomputedGraph = (PrecomputedRuleGraph) implClass.getConstructor().newInstance();

            log.info("Precomputed rule graph loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load precomputed rule graph", e);
            throw new IllegalStateException("Precomputed rule graph not available", e);
        }
    }

    /**
     * Discovers and registers only the rule beans that are referenced in the precomputed graph.
     * <p>
     * This method implements a key optimization in the rule engine: it only loads rule
     * implementations that are actually needed based on the precomputed graph. This approach
     * significantly reduces memory usage and startup time, especially in large systems with
     * many rule implementations where only a subset might be used for specific tasks.
     * <p>
     * The method works in three steps:
     * <ol>
     *   <li>Extract all unique rule class names from the precomputed graph</li>
     *   <li>Query the Spring context for all {@link IRule} implementations</li>
     *   <li>Register only those rule implementations that are referenced in the graph</li>
     * </ol>
     * <p>
     * Additionally, this method provides diagnostic information about missing rules, which
     * can help identify configuration issues early during application startup rather than
     * failing at runtime.
     */
    private void discoverRequiredRules() {
        log.info("Discovering required rule implementations...");

        // Extract all unique rule class names from the precomputed graph and track their discovery status
        // This creates a map where keys are class names and values are booleans indicating if they've been found
        Map<String, Boolean> requiredRuleClasses = precomputedGraph.getSortedRulesByTaskAndType()
                .values()                                // Get all target type maps
                .stream()                                // Stream the maps
                .flatMap(targetTypeMap -> targetTypeMap.values().stream())  // Flatten to all rule lists
                .flatMap(List::stream)                  // Flatten to individual rule class names
                .collect(Collectors.toMap(
                        className -> className,          // Key is the class name
                        className -> false,             // Initial value false (not yet found)
                        (existing, replacement) -> existing  // Keep existing entry on duplicate key
                ));

        log.debug("Precomputed graph references {} unique rule classes", requiredRuleClasses.size());

        // Find and register only the required rule beans
        beanFactory.getBeansOfType(IRule.class)
                .forEach((beanName, ruleBean) -> {
                    String className = ruleBean.getClass().getName();

                    // Only register if this rule is referenced in the precomputed graph
                    if (requiredRuleClasses.containsKey(className)) {
                        ruleRegistry.put(className, ruleBean);
                        requiredRuleClasses.put(className, true); // mark as found
                        log.debug("Registered required rule: {}", className);
                    } else {
                        log.debug("Skipping unreferenced rule: {}", className);
                    }
                });

        // Log any missing rules
        long missingRules = requiredRuleClasses.values().stream()
                .mapToLong(found -> found ? 0 : 1)
                .sum();

        if (missingRules > 0) {
            log.warn("{} rules referenced in precomputed graph were not found in Spring context", missingRules);

            if (log.isDebugEnabled()) {
                requiredRuleClasses.entrySet().stream()
                        .filter(entry -> !entry.getValue())
                        .forEach(entry -> log.debug("Missing rule: {}", entry.getKey()));
            }
        }

        log.info("Discovered {} required rule implementations", ruleRegistry.size());
    }

    /**
     * Executes all applicable rules for the given task and element.
     * <p>
     * This method is the primary entry point for rule execution. It retrieves the correct
     * sequence of rules from the precomputed graph based on the task and element type,
     * then executes them in parallel while respecting their dependency order.
     * <p>
     * The method follows these steps:
     * <ol>
     *   <li>Identify the correct set of rule class names from the precomputed graph</li>
     *   <li>Execute those rules in parallel using {@link CompletableFuture}</li>
     *   <li>Merge the results from all rules into a single result object</li>
     * </ol>
     * <p>
     * If no rules are found for the given task and element type, an empty result is returned.
     * <p>
     * <strong>Example usage:</strong>
     * <pre>
     * {@code
     * // Validate an Operation
     * ValidationResult validationResult = ruleService.executeRules(
     *     RuleTask.VALIDATE,
     *     operation,
     *     ValidationResult::new
     * );
     *
     * // Enrich a TypeProfile
     * EnrichmentResult enrichmentResult = ruleService.executeRules(
     *     RuleTask.ENRICH,
     *     typeProfile,
     *     EnrichmentResult::new
     * );
     * }
     * </pre>
     *
     * @param task          the rule task to execute (e.g., {@link RuleTask#VALIDATE})
     * @param element       the domain element to process
     * @param resultFactory factory for creating result objects (typically a constructor reference)
     * @param <T>           element type extending {@link VisitableElement}
     * @param <R>           result type extending {@link RuleOutput}
     * @return the merged result of all executed rules, or an empty result if no rules apply
     * @throws RuntimeException if a critical error occurs during rule execution that prevents
     *                          completion of the operation
     */
    public <T extends VisitableElement, R extends RuleOutput<R>> R executeRules(
            RuleTask task,
            T element,
            Supplier<R> resultFactory
    ) {
        String elementType = element.getClass().getName();
        log.debug("Executing rules for task={}, elementType={}", task, elementType);

        // Get sorted rule class names directly from precomputed graph
        List<String> ruleClassNames = precomputedGraph.getSortedRuleClasses(task, elementType);

        if (ruleClassNames.isEmpty()) {
            log.debug("No rules found for task={}, elementType={}", task, elementType);
            return resultFactory.get();
        }

        log.debug("Found {} rules for task={}, elementType={}", ruleClassNames.size(), task, elementType);

        // Execute rules in parallel and merge results
        return executeRulesInParallel(ruleClassNames, element, resultFactory);
    }

    /**
     * Executes rules in parallel while respecting their precomputed ordering.
     * <p>
     * This method is responsible for the actual parallel execution of rules. It takes the
     * list of rule class names in their precomputed execution order, retrieves the rule
     * instances from the registry, and executes them concurrently.
     * <p>
     * Key aspects of this implementation:
     * <ul>
     *   <li><strong>Selective execution</strong>: Only rules that are found in the registry are executed</li>
     *   <li><strong>Parallel processing</strong>: Each rule executes in its own {@link CompletableFuture}</li>
     *   <li><strong>Coordinated completion</strong>: The method waits for all rule executions to complete</li>
     *   <li><strong>Result aggregation</strong>: Results from all rules are merged into a single result</li>
     * </ul>
     * <p>
     * If no rule instances are available for execution, an empty result is returned. This ensures
     * that the method always returns a valid result object even if no rules are executed.
     *
     * @param ruleClassNames the list of rule class names in execution order
     * @param element        the domain element to process
     * @param resultFactory  factory for creating result objects
     * @param <T>            element type extending VisitableElement
     * @param <R>            result type extending RuleOutput
     * @return the merged result of all executed rules
     * @throws RuntimeException if rule execution fails critically
     */
    private <T extends VisitableElement, R extends RuleOutput<R>> R executeRulesInParallel(
            List<String> ruleClassNames,
            T element,
            Supplier<R> resultFactory
    ) {
        // Create result futures for rule execution, but only for rules that are actually available in the registry
        // This pipeline: 1) Gets rule instances from registry, 2) Filters out missing rules, 3) Executes each rule asynchronously
        List<CompletableFuture<R>> resultFutures = ruleClassNames.stream()
                .map(ruleRegistry::get)                  // Look up each rule by class name
                .filter(Objects::nonNull)                // Skip rules that weren't found (null)
                .map(rule -> executeRule(rule, element, resultFactory))  // Execute each rule asynchronously
                .collect(Collectors.toList());          // Collect all future results

        if (resultFutures.isEmpty()) {
            log.debug("No available rule instances found for execution");
            return resultFactory.get();
        }

        // Wait for all executions to complete
        try {
            CompletableFuture.allOf(resultFutures.toArray(CompletableFuture[]::new)).join();
        } catch (Exception e) {
            log.error("Error during rule execution", e);
            throw new RuntimeException("Rule execution failed", e);
        }

        // Merge results
        return mergeResults(resultFutures, resultFactory);
    }

    /**
     * Executes a single rule asynchronously and returns its result future.
     * <p>
     * This method wraps the execution of an individual rule in a {@link CompletableFuture} to
     * enable asynchronous processing. It handles the lifecycle of rule execution including:
     * <ul>
     *   <li>Creating a fresh result object for the rule</li>
     *   <li>Safely casting the rule to the correct parameterized type</li>
     *   <li>Invoking the rule's processing logic</li>
     *   <li>Capturing and handling any exceptions that occur during execution</li>
     * </ul>
     * <p>
     * The type casting in this method is safe because the precomputed graph ensures that
     * rules are only invoked with compatible element and result types. The {@code @SuppressWarnings}
     * annotation is used to silence the compiler warning about this cast.
     *
     * @param rule          the rule implementation to execute
     * @param element       the domain element to process
     * @param resultFactory factory for creating result objects
     * @param <T>           element type extending VisitableElement
     * @param <R>           result type extending RuleOutput
     * @return a CompletableFuture containing the rule's execution result
     */
    @SuppressWarnings("unchecked")
    private <T extends VisitableElement, R extends RuleOutput<R>> CompletableFuture<R> executeRule(
            IRule<?, ?> rule,
            T element,
            Supplier<R> resultFactory
    ) {
        return CompletableFuture.supplyAsync(() -> {
            String ruleName = rule.getClass().getSimpleName();
            log.debug("Executing rule: {}", ruleName);

            R result = resultFactory.get();

            try {
                // Perform a type-safe cast to the specific generic parameter types needed for this rule execution
                // This cast is guaranteed to be safe because the precomputed graph ensures type compatibility
                IRule<T, R> typedRule = (IRule<T, R>) rule;

                // Execute the rule's processing logic with the input element and result container
                typedRule.process(element, result);
                log.debug("Rule {} execution completed successfully", ruleName);
                return result;
            } catch (Exception e) {
                log.error("Rule {} execution failed: {}", ruleName, e.getMessage(), e);
                throw new RuntimeException("Rule execution failed: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Merges results from multiple rule executions into a single consolidated result.
     * <p>
     * This method aggregates the results from all rule executions into a single result object.
     * It handles several important aspects of result processing:
     * <ul>
     *   <li><strong>Failure resilience</strong>: Gracefully handles failures in individual rule executions</li>
     *   <li><strong>Type safety</strong>: Creates properly typed arrays for the merge operation</li>
     *   <li><strong>Empty result handling</strong>: Returns a valid empty result when no results are available</li>
     * </ul>
     * <p>
     * The merge operation itself is delegated to the {@link RuleOutput#merge} method of the result type,
     * which implements domain-specific logic for combining multiple results. This allows for
     * flexible result aggregation strategies depending on the specific rule task.
     *
     * @param resultFutures futures containing the results of rule executions
     * @param resultFactory factory for creating the initial result object
     * @param <R>           result type extending RuleOutput
     * @return a single merged result containing the combined output of all rules
     */
    private <R extends RuleOutput<R>> R mergeResults(
            List<CompletableFuture<R>> resultFutures,
            Supplier<R> resultFactory
    ) {
        R finalResult = resultFactory.get();

        if (resultFutures.isEmpty()) {
            return finalResult;
        }

        log.debug("Merging {} rule results", resultFutures.size());

        // Extract results from all futures, gracefully handling any that failed during execution
        // This creates a list containing only the successfully completed rule results
        List<R> successfulResults = resultFutures.stream()
                .map(future -> {
                    try {
                        // Get the result from the completed future
                        return future.join();
                    } catch (Exception e) {
                        // If the future completed exceptionally, log a warning and return null
                        // This prevents failures in one rule from affecting the entire merge process
                        log.warn("Skipping failed rule result during merge: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)  // Remove any nulls from failed rule executions
                .toList();

        if (successfulResults.isEmpty()) {
            return finalResult;
        }

        // Create a properly typed array for the merge operation using reflection
        // This is necessary because Java's type erasure prevents direct instantiation of generic arrays
        // We use the concrete class of the first result to determine the array component type
        @SuppressWarnings("unchecked")
        R[] resultsArray = successfulResults.toArray((R[]) Array.newInstance(
                successfulResults.getFirst().getClass(), successfulResults.size()));

        return finalResult.merge(resultsArray);
    }
}