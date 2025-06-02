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

import edu.kit.datamanager.idoris.domain.VisitableElement;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Central component that replaces the former "registry" + "engine" duo.
 * <p>
 * Responsibilities:
 * <ol>
 *   <li>Discover all Spring beans annotated with {@link Rule}.</li>
 *   <li>Build an index "task → element-type → topologically sorted rules".</li>
 *   <li>Execute the rules for a given input while respecting declared dependencies.</li>
 * </ol>
 * <p>
 * Threading model: each rule is executed asynchronously (via {@link CompletableFuture})
 * once all of its declared {@link Rule#dependsOn() dependencies} are finished.
 * This allows parallel execution when rules are independent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RuleService {

    /**
     * Provides access to all Spring-managed beans for rule discovery
     */
    private final ListableBeanFactory beanFactory;

    /**
     * Immutable read-only index built during initialization.
     * Structure: RuleTask → ElementType → TopologicallySortedRules
     * <pre>
     * ┌─────────────┐
     * │  RuleTask   │ ─▶ element-type (class) ─▶ ordered list of rules
     * └─────────────┘
     * </pre>
     */
    private final Map<RuleTask, Map<Class<? extends VisitableElement>, List<RuleNode>>> rulesByTaskAndType = new HashMap<>();

    /* --------------------------------------------------------------------- */
    /* ------------------------- Initialization ---------------------------- */
    /* --------------------------------------------------------------------- */

    /**
     * Sorts rules according to their {@link Rule#dependsOn()} declarations using topological sort.
     * Detects and throws {@link IllegalStateException} for cyclic dependencies.
     *
     * @param ruleMap map of rule class to rule node
     * @return topologically sorted list of rule nodes
     * @throws IllegalStateException if cyclic dependencies are detected
     */
    private static List<RuleNode> topologicallySort(Map<Class<?>, RuleNode> ruleMap) {
        Map<Class<?>, List<Class<?>>> dependencyGraph = buildDependencyGraph(ruleMap);

        List<RuleNode> sortedRules = new ArrayList<>();
        Map<Class<?>, VisitState> visitStates = new HashMap<>();

        // Visit all rule classes to ensure complete traversal
        for (Class<?> ruleClass : ruleMap.keySet()) {
            if (visitStates.get(ruleClass) == null) {
                performDepthFirstSearch(ruleClass, dependencyGraph, visitStates, sortedRules, ruleMap);
            }
        }

        return sortedRules;
    }

    /**
     * Builds a dependency graph from rule nodes.
     */
    private static Map<Class<?>, List<Class<?>>> buildDependencyGraph(Map<Class<?>, RuleNode> ruleMap) {
        Map<Class<?>, List<Class<?>>> dependencyGraph = new HashMap<>();

        ruleMap.values().forEach(ruleNode -> {
            Class<?> ruleClass = ruleNode.instance().getClass();
            List<Class<?>> dependencies = Arrays.asList(ruleNode.annotation().dependsOn());
            dependencyGraph.put(ruleClass, dependencies);
        });

        return dependencyGraph;
    }

    /**
     * Performs depth-first search for topological sorting.
     * Detects cycles and maintains proper ordering.
     */
    private static void performDepthFirstSearch(Class<?> currentRuleClass,
                                                Map<Class<?>, List<Class<?>>> dependencyGraph,
                                                Map<Class<?>, VisitState> visitStates,
                                                List<RuleNode> sortedRules,
                                                Map<Class<?>, RuleNode> ruleMap) {
        visitStates.put(currentRuleClass, VisitState.VISITING);

        // Process all dependencies
        List<Class<?>> dependencies = dependencyGraph.getOrDefault(currentRuleClass, Collections.emptyList());
        for (Class<?> dependencyClass : dependencies) {
            VisitState dependencyState = visitStates.get(dependencyClass);

            if (dependencyState == VisitState.VISITING) {
                throw new IllegalStateException(
                        String.format("Cyclic rule dependency detected: %s <-> %s",
                                currentRuleClass.getSimpleName(), dependencyClass.getSimpleName()));
            }

            if (dependencyState == null) {
                performDepthFirstSearch(dependencyClass, dependencyGraph, visitStates, sortedRules, ruleMap);
            }
        }

        visitStates.put(currentRuleClass, VisitState.VISITED);

        // Add to result only if the rule exists in our rule map
        RuleNode ruleNode = ruleMap.get(currentRuleClass);
        if (ruleNode != null) {
            sortedRules.add(ruleNode);
        }
    }

    /**
     * Builds precedence graph for scheduling execution order.
     */
    private static Map<Class<?>, List<Class<?>>> buildPrecedenceGraph(List<RuleNode> ruleNodes) {
        Map<Class<?>, List<Class<?>>> precedenceGraph = new HashMap<>();

        for (RuleNode ruleNode : ruleNodes) {
            Class<?> ruleClass = ruleNode.instance().getClass();
            List<Class<?>> dependencies = new ArrayList<>();
            Collections.addAll(dependencies, ruleNode.annotation().dependsOn());
            precedenceGraph.put(ruleClass, dependencies);
        }

        return precedenceGraph;
    }

    /* --------------------------------------------------------------------- */
    /* ----------------------- Topological Sorting ------------------------ */
    /* --------------------------------------------------------------------- */

    /**
     * Called by Spring after bean construction to initialize the rule index.
     * Discovers all {@link Rule @Rule} annotated beans and builds the internal indices.
     */
    @PostConstruct
    void init() {
        log.info("Initializing RuleService - discovering rule beans...");
        Collection<Object> ruleBeans = beanFactory.getBeansWithAnnotation(Rule.class).values();
        log.info("Found {} rule beans", ruleBeans.size());
        buildRuleIndex(ruleBeans);
        log.info("RuleService initialization complete");
    }

    /**
     * Builds the {@link #rulesByTaskAndType} index and topologically sorts rules.
     *
     * @param ruleBeans discovered Spring beans annotated with {@link Rule}
     */
    private void buildRuleIndex(Collection<Object> ruleBeans) {
        // Temporary structure for collecting rules before topological sorting
        Map<RuleTask, Map<Class<? extends VisitableElement>, Map<Class<?>, RuleNode>>> ruleCollector = new HashMap<>();

        // Phase 1: Group rules by task and element type
        collectRulesByTaskAndType(ruleBeans, ruleCollector);

        // Phase 2: Topologically sort rules for each (task, type) combination
        applySortingToCollectedRules(ruleCollector);
    }

    /**
     * Groups discovered rule beans by their declared tasks and applicable element types.
     */
    private void collectRulesByTaskAndType(Collection<Object> ruleBeans,
                                           Map<RuleTask, Map<Class<? extends VisitableElement>, Map<Class<?>, RuleNode>>> collector) {
        for (Object ruleBean : ruleBeans) {
            Rule ruleAnnotation = ruleBean.getClass().getAnnotation(Rule.class);
            if (ruleAnnotation == null) {
                log.warn("Bean {} was retrieved as @Rule annotated but annotation is null", ruleBean.getClass().getSimpleName());
                continue;
            }

            RuleNode ruleNode = new RuleNode(ruleBean, ruleAnnotation);

            // Register rule for each declared task and element type combination
            for (RuleTask task : ruleAnnotation.tasks()) {
                for (Class<? extends VisitableElement> elementType : ruleAnnotation.appliesTo()) {
                    collector
                            .computeIfAbsent(task, k -> new HashMap<>())
                            .computeIfAbsent(elementType, k -> new HashMap<>())
                            .put(ruleBean.getClass(), ruleNode);
                }
            }
        }
    }

    /* --------------------------------------------------------------------- */
    /* ----------------------------- Public API ---------------------------- */
    /* --------------------------------------------------------------------- */

    /**
     * Applies topological sorting to all collected rule groups.
     */
    private void applySortingToCollectedRules(Map<RuleTask, Map<Class<? extends VisitableElement>, Map<Class<?>, RuleNode>>> collector) {
        collector.forEach((task, rulesByElementType) -> {
            Map<Class<? extends VisitableElement>, List<RuleNode>> sortedRulesByElementType = new HashMap<>();

            rulesByElementType.forEach((elementType, ruleMap) -> {
                List<RuleNode> sortedRules = topologicallySort(ruleMap);
                sortedRulesByElementType.put(elementType, sortedRules);
                log.debug("Sorted {} rules for task {} and element type {}",
                        sortedRules.size(), task, elementType.getSimpleName());
            });

            rulesByTaskAndType.put(task, sortedRulesByElementType);
        });
    }

    /**
     * Executes all applicable rules for the given task and element.
     * Rules are executed asynchronously with proper dependency ordering.
     *
     * @param task           the logical task (e.g., VALIDATE, ENRICH)
     * @param input          the domain element to process
     * @param resultSupplier factory for creating fresh result objects
     * @param <T>            element type extending VisitableElement
     * @param <R>            result type extending RuleOutput
     * @return the merged result of all executed rules
     */
    public <T extends VisitableElement, R extends RuleOutput<R>> R process(
            RuleTask task,
            T input,
            Supplier<R> resultSupplier
    ) {
        List<RuleNode> applicableRules = getRulesForTaskAndType(task, input.getClass());

        if (applicableRules.isEmpty()) {
            log.debug("No rules found for task {} and element type {}", task, input.getClass().getSimpleName());
            return resultSupplier.get();
        }

        log.debug("Processing {} rules for task {} and element type {}",
                applicableRules.size(), task, input.getClass().getSimpleName());

        return executeRulesAsync(applicableRules, input, resultSupplier);
    }

    /**
     * Retrieves applicable rules for a specific task and element type.
     */
    private List<RuleNode> getRulesForTaskAndType(RuleTask task, Class<? extends VisitableElement> elementType) {
        return rulesByTaskAndType
                .getOrDefault(task, Collections.emptyMap())
                .getOrDefault(elementType, Collections.emptyList());
    }

    /**
     * Executes rules asynchronously while respecting dependencies.
     */
    private <T extends VisitableElement, R extends RuleOutput<R>> R executeRulesAsync(
            List<RuleNode> ruleNodes,
            T input,
            Supplier<R> resultSupplier
    ) {
        // Build execution structures
        Map<Class<?>, RuleNode> ruleNodeMap = createRuleNodeMap(ruleNodes);
        Map<Class<?>, List<Class<?>>> dependencyGraph = buildPrecedenceGraph(ruleNodes);
        Map<Class<?>, CompletableFuture<R>> executionFutures = new ConcurrentHashMap<>();

        // Schedule all rules for execution and collect their futures
        List<CompletableFuture<R>> scheduledFutures = dependencyGraph.keySet().stream()
                .map(ruleClass -> scheduleRuleExecution(ruleClass, dependencyGraph, ruleNodeMap, executionFutures, input, resultSupplier))
                .toList();

        // Wait for all executions to complete
        CompletableFuture<Void> allCompleted = CompletableFuture.allOf(
                scheduledFutures.toArray(CompletableFuture[]::new));

        try {
            allCompleted.join();
        } catch (Exception e) {
            log.error("Error during rule execution", e);
            throw new RuntimeException("Rule execution failed", e);
        }

        // Merge all results deterministically
        return mergeResults(scheduledFutures, resultSupplier);
    }

    /**
     * Creates a map from rule class to rule node for quick lookup.
     */
    private Map<Class<?>, RuleNode> createRuleNodeMap(List<RuleNode> ruleNodes) {
        return ruleNodes.stream()
                .collect(Collectors.toUnmodifiableMap(
                        node -> node.instance().getClass(),
                        Function.identity()));
    }

    /**
     * Recursively schedules rule execution, ensuring dependencies are executed first.
     * Uses memoization to avoid duplicate scheduling.
     */
    private <T extends VisitableElement, R extends RuleOutput<R>> CompletableFuture<R> scheduleRuleExecution(
            Class<?> ruleClass,
            Map<Class<?>, List<Class<?>>> dependencyGraph,
            Map<Class<?>, RuleNode> ruleNodeMap,
            Map<Class<?>, CompletableFuture<R>> executionFutures,
            T input,
            Supplier<R> resultSupplier
    ) {
        return executionFutures.computeIfAbsent(ruleClass, rc -> {
            // Schedule all dependencies first
            List<CompletableFuture<R>> dependencyFutures = dependencyGraph
                    .getOrDefault(rc, Collections.emptyList())
                    .stream()
                    .map(dependencyClass -> scheduleRuleExecution(
                            dependencyClass, dependencyGraph, ruleNodeMap, executionFutures, input, resultSupplier))
                    .toList();

            // Execute rule after all dependencies complete
            return CompletableFuture
                    .allOf(dependencyFutures.toArray(CompletableFuture[]::new))
                    .thenApplyAsync(ignored -> executeRuleWithDependencies(
                            ruleClass, dependencyFutures, ruleNodeMap, input, resultSupplier))
                    .exceptionally(throwable -> {
                        log.error("Error executing rule {}", ruleClass.getSimpleName(), throwable);
                        throw new RuntimeException("Rule execution failed for " + ruleClass.getSimpleName(), throwable);
                    });
        });
    }

    /**
     * Executes a single rule after merging results from its dependencies.
     */
    private <T extends VisitableElement, R extends RuleOutput<R>> R executeRuleWithDependencies(
            Class<?> ruleClass,
            List<CompletableFuture<R>> dependencyFutures,
            Map<Class<?>, RuleNode> ruleNodeMap,
            T input,
            Supplier<R> resultSupplier
    ) {
        // Merge dependency results
        R aggregatedResult = mergeResults(dependencyFutures, resultSupplier);

        // Execute the rule itself
        RuleNode ruleNode = ruleNodeMap.get(ruleClass);
        if (ruleNode != null) {
            try {
                @SuppressWarnings("unchecked")
                RuleProcessor<T, R> processor = (RuleProcessor<T, R>) ruleNode.instance();
                processor.process(input, aggregatedResult);
                log.debug("Successfully executed rule {}", ruleClass.getSimpleName());
            } catch (Exception e) {
                log.error("Error in rule execution for {}", ruleClass.getSimpleName(), e);
                throw new RuntimeException("Rule execution failed", e);
            }
        }

        return aggregatedResult;
    }

    /**
     * Merges multiple rule results into a single result.
     * Properly handles the generic types to avoid unchecked warnings.
     */
    private <R extends RuleOutput<R>> R mergeResults(Collection<CompletableFuture<R>> futures, Supplier<R> resultSupplier) {
        return futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (Exception e) {
                        log.error("Error joining future result", e);
                        throw new RuntimeException("Failed to merge rule results", e);
                    }
                })
                .reduce(resultSupplier.get(), (accumulator, result) -> {
                    try {
                        if (result == null) {
                            return accumulator; // Skip null results
                        }
                        return accumulator;
                    } catch (Exception e) {
                        log.error("Error merging rule results", e);
                        throw new RuntimeException("Failed to merge rule results", e);
                    }
                });
    }

    /* --------------------------------------------------------------------- */
    /* --------------------------- Inner Classes --------------------------- */
    /* --------------------------------------------------------------------- */

    /**
     * Enumeration for tracking DFS visit states during topological sorting.
     */
    private enum VisitState {
        /**
         * Currently being visited (on the DFS stack)
         */
        VISITING,
        /**
         * Completely visited and processed
         */
        VISITED
    }

    /**
     * Immutable record that pairs a Spring bean instance with its {@link Rule} annotation.
     * Provides convenient access to both the executable rule and its metadata.
     *
     * @param instance   the Spring bean implementing {@link RuleProcessor}
     * @param annotation the {@link Rule} annotation containing metadata
     */
    public record RuleNode(Object instance, Rule annotation) {
        public RuleNode {
            Objects.requireNonNull(instance, "Rule instance cannot be null");
            Objects.requireNonNull(annotation, "Rule annotation cannot be null");
        }
    }
}