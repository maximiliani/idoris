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

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compile-time annotation processor for validating {@link Rule} annotated classes.
 *
 * <p>This processor ensures that rule classes are properly structured and that their
 * dependencies form a valid directed acyclic graph (DAG). It performs comprehensive
 * validation at compile time to catch configuration errors early in the development cycle.
 *
 * <h2>Validation Features</h2>
 * <ul>
 *   <li><strong>Structural Validation:</strong> Ensures rule classes implement {@link IRule}
 *       and are properly annotated</li>
 *   <li><strong>Type Safety:</strong> Validates that target types extend {@link VisitableElement}
 *       and are not abstract classes</li>
 *   <li><strong>Dependency Validation:</strong> Detects circular dependencies between rules
 *       using topological sorting</li>
 *   <li><strong>Task Compatibility:</strong> Ensures dependencies exist within the same
 *       task and target type context</li>
 * </ul>
 *
 * <h2>Processing Algorithm</h2>
 * <p>The processor operates in two distinct phases:
 * <ol>
 *   <li><strong>Collection Phase:</strong> Scans all {@code @Rule} annotated classes across
 *       multiple compilation rounds, extracting and validating their metadata</li>
 *   <li><strong>Validation Phase:</strong> After all source files are processed, validates
 *       the complete dependency graph for cycles and inconsistencies</li>
 * </ol>
 *
 * <h2>Dependency Graph Structure</h2>
 * <p>Rules are organized into separate dependency graphs based on:
 * <ul>
 *   <li><strong>Task Type:</strong> Rules for different tasks (VALIDATE, CONSUME, etc.)
 *       are validated independently</li>
 *   <li><strong>Target Type:</strong> Rules targeting different visitable element types
 *       are validated in separate graphs</li>
 * </ul>
 *
 * <p>This separation ensures that dependency validation is contextually appropriate
 * and prevents false positives from cross-context dependencies.
 *
 * <h2>Error Reporting</h2>
 * <p>The processor reports compilation errors for:
 * <ul>
 *   <li>Classes that don't implement {@link IRule}</li>
 *   <li>Rules targeting non-{@link VisitableElement} types</li>
 *   <li>Rules targeting abstract classes</li>
 *   <li>Circular dependencies between rules</li>
 *   <li>Missing required annotation attributes</li>
 * </ul>
 *
 * @see Rule
 * @see IRule
 * @see VisitableElement
 * @see RuleTask
 * @since 1.0
 */
@SupportedAnnotationTypes("edu.kit.datamanager.idoris.rules.logic.Rule")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public final class RuleAnnotationProcessor extends AbstractProcessor {

    /**
     * Three-level nested map structure organizing collected rule metadata.
     *
     * <p>Structure: {@code Task → TargetType → RuleClassName → RuleMetadata}
     *
     * <p>This organization allows for independent validation of dependency graphs
     * within each (task, target-type) combination, ensuring that rules can only
     * depend on other rules that:
     * <ul>
     *   <li>Execute in the same task context</li>
     *   <li>Process the same target type</li>
     * </ul>
     *
     * <p>Example structure:
     * <pre>
     * VALIDATE → {
     *   "com.example.MyEntity" → {
     *     "com.example.ValidationRule1" → RuleMetadata(...),
     *     "com.example.ValidationRule2" → RuleMetadata(...)
     *   }
     * }
     * CONSUME → {
     *   "com.example.MyEntity" → {
     *     "com.example.ConsumptionRule" → RuleMetadata(...)
     *   }
     * }
     * </pre>
     */
    private final Map<RuleTask, Map<String, Map<String, RuleMetadata>>> rulesByTaskAndTarget = new HashMap<>();
    /**
     * Utility for performing type-related operations during annotation processing.
     * Used for type assignability checks and type comparisons.
     */
    private Types typeUtilities;
    /**
     * Utility for working with program elements during annotation processing.
     * Used for looking up type elements and accessing element metadata.
     */
    private Elements elementUtilities;

    /**
     * Initializes the annotation processor with necessary utilities from the processing environment.
     *
     * <p>This method is called once by the compiler before any processing begins.
     * It sets up the type and element utilities needed for reflection-like operations
     * during annotation processing.
     *
     * @param processingEnvironment the processing environment provided by the compiler,
     *                              containing utilities and configuration for this processing session
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        // Initialize utilities for type operations (assignability, erasure, etc.)
        this.typeUtilities = processingEnvironment.getTypeUtils();
        // Initialize utilities for element operations (lookup, metadata access, etc.)
        this.elementUtilities = processingEnvironment.getElementUtils();
    }

    /**
     * Main processing method implementing the two-phase validation strategy.
     *
     * <p>This method is called by the compiler for each round of annotation processing.
     * The compiler may invoke this method multiple times as it processes different
     * source files and generates new ones.
     *
     * <h3>Phase 1: Collection (Normal Rounds)</h3>
     * <p>During normal processing rounds ({@code processingRound.processingOver() == false}):
     * <ul>
     *   <li>Scan all {@code @Rule} annotated classes found in this round</li>
     *   <li>Validate basic structural requirements (implements IRule, etc.)</li>
     *   <li>Extract and store rule metadata for dependency validation</li>
     * </ul>
     *
     * <h3>Phase 2: Validation (Final Round)</h3>
     * <p>During the final round ({@code processingRound.processingOver() == true}):
     * <ul>
     *   <li>All source files have been processed</li>
     *   <li>Complete rule dependency graphs are available</li>
     *   <li>Perform topological sort to detect cycles</li>
     *   <li>Report any circular dependencies as compilation errors</li>
     * </ul>
     *
     * @param annotationTypes the set of annotation types requested to be processed
     * @param processingRound the environment for this round of processing
     * @return {@code false} to allow other annotation processors to process the same annotations
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotationTypes, RoundEnvironment processingRound) {
        if (processingRound.processingOver()) {
            // Final round: all source files processed, validate complete dependency graphs
            // This is executed after all validation rounds by the compiler are complete
            validateAllRuleDependencyGraphs();
            return false;
        }

        // Normal round: collect rule metadata from newly processed source files
        collectRuleMetadataFromAnnotatedClasses(processingRound);
        return false; // Allow other processors to see the same annotations
    }

    /**
     * Collects metadata from all {@code @Rule} annotated classes in the current processing round.
     *
     * <p>This method iterates through all elements annotated with {@code @Rule} that were
     * discovered in the current compilation round. For each valid rule class, it:
     * <ol>
     *   <li>Validates the class structure (implements IRule, is a class, etc.)</li>
     *   <li>Extracts rule metadata (dependencies, target types, tasks)</li>
     *   <li>Stores the metadata for later dependency validation</li>
     * </ol>
     *
     * <p>Invalid elements (non-classes) are skipped with a warning, allowing processing
     * to continue for valid rules.
     *
     * @param processingRound the current processing round containing newly discovered annotated elements
     */
    private void collectRuleMetadataFromAnnotatedClasses(RoundEnvironment processingRound) {
        // Get all elements annotated with @Rule in this processing round
        // Phase 1: Collection - scan all @Rule annotated elements in this compilation round
        Set<? extends Element> ruleAnnotatedElements = processingRound.getElementsAnnotatedWith(Rule.class);

        for (Element annotatedElement : ruleAnnotatedElements) {
            // Only process type elements (classes, interfaces, enums)
            // Skip other elements like methods, fields, etc.
            if (annotatedElement instanceof TypeElement ruleClass) {
                // Validate that the class meets structural requirements
                validateRuleClassStructure(ruleClass);

                // Extract and store rule metadata for dependency validation
                collectRuleMetadata(ruleClass);
            }
            // Non-type elements are silently skipped - they'll be caught by other validation
        }
    }

    /**
     * Validates that a rule class meets all structural requirements.
     *
     * <p>This method performs comprehensive validation of a rule class to ensure it:
     * <ul>
     *   <li>Is actually a class (not an interface, enum, or annotation)</li>
     *   <li>Implements the required {@link IRule} interface</li>
     * </ul>
     *
     * <p>Any violations are reported as compilation errors, preventing the build
     * from succeeding with invalid rule configurations.
     *
     * @param ruleClass the type element representing the rule class to validate
     */
    private void validateRuleClassStructure(TypeElement ruleClass) {
        // Ensure the annotated element is a class
        validateIsClass(ruleClass);

        // Ensure the class implements the IRule interface
        validateImplementsIRule(ruleClass);
    }

    /**
     * Validates that the annotated element is a class.
     *
     * <p>The {@code @Rule} annotation should only be applied to classes, not to
     * interfaces, enums, annotations, or other element types. This validation
     * catches misuse of the annotation early in the compilation process.
     *
     * @param typeElement the type element to validate
     */
    private void validateIsClass(TypeElement typeElement) {
        // Check if the annotated element is actually a class
        if (typeElement.getKind() != ElementKind.CLASS) {
            reportError(typeElement,
                    "Rule annotation can only be applied to classes, not %s. " +
                            "Found annotation on %s which is a %s.",
                    typeElement.getKind(),
                    typeElement.getQualifiedName(),
                    typeElement.getKind());
        }
    }

    /**
     * Validates that the rule class implements the {@link IRule} interface.
     *
     * <p>All rule classes must implement the {@code IRule} interface to provide
     * the required {@code process} method. This validation ensures type safety
     * and proper integration with the rule execution framework.
     *
     * <p>The validation uses type assignability checking to handle generic types
     * and inheritance hierarchies correctly.
     *
     * @param ruleClass the rule class to validate
     */
    private void validateImplementsIRule(TypeElement ruleClass) {
        if (!implementsIRule(ruleClass)) {
            reportError(ruleClass,
                    "Rule class %s must implement IRule interface. " +
                            "Add 'implements IRule<YourTargetType, YourOutputType>' to the class declaration.",
                    ruleClass.getQualifiedName());
        }
    }

    /**
     * Checks if a type implements the {@link IRule} interface.
     *
     * <p>This method uses the type utilities to perform assignability checking,
     * which correctly handles:
     * <ul>
     *   <li>Direct implementation of IRule</li>
     *   <li>Implementation through inheritance</li>
     *   <li>Generic type parameters</li>
     *   <li>Raw types and type erasure</li>
     * </ul>
     *
     * @param candidateType the type to check for IRule implementation
     * @return {@code true} if the type implements IRule, {@code false} otherwise
     */
    private boolean implementsIRule(TypeElement candidateType) {
        // Look up the IRule interface in the current compilation context
        TypeElement iRuleInterface = elementUtilities.getTypeElement("edu.kit.datamanager.idoris.rules.logic.IRule");

        if (iRuleInterface == null) {
            // IRule interface not found in classpath - this suggests a configuration problem
            // Return false to trigger validation error
            return false;
        }

        // Use assignability checking to handle generics and inheritance
        // erasure() is used to handle raw types correctly
        // Check if the type is assignable to IRule (handles inheritance and generics)
        return typeUtilities.isAssignable(
                candidateType.asType(),
                typeUtilities.erasure(iRuleInterface.asType())
        );
    }

    /**
     * Extracts and stores comprehensive metadata from a rule class.
     *
     * <p>This method performs the core metadata extraction process:
     * <ol>
     *   <li><strong>Annotation Access:</strong> Safely retrieves the {@code @Rule} annotation
     *       using annotation mirrors to avoid {@code MirroredTypesException}</li>
     *   <li><strong>Attribute Extraction:</strong> Extracts all annotation attributes
     *       (tasks, appliesTo, dependsOn, executeBefore)</li>
     *   <li><strong>Validation:</strong> Validates target types and dependencies</li>
     *   <li><strong>Storage:</strong> Organizes the metadata for efficient dependency validation</li>
     * </ol>
     *
     * <p>The extracted metadata is stored in the {@link #rulesByTaskAndTarget} structure,
     * organized by task and target type to enable independent validation of each
     * dependency graph.
     *
     * @param ruleClass the rule class to extract metadata from
     */
    private void collectRuleMetadata(TypeElement ruleClass) {
        // Safely retrieve the @Rule annotation mirror to avoid MirroredTypesException
        AnnotationMirror ruleAnnotation = findRuleAnnotation(ruleClass);
        if (ruleAnnotation == null) {
            // This should not happen since we're processing @Rule annotated elements
            // but we handle it defensively
            return;
        }

        // Extract all annotation attributes as a map for easy access
        Map<String, AnnotationValue> annotationAttributes = extractAnnotationAttributes(ruleAnnotation);

        // Extract and validate the tasks this rule applies to
        RuleTask[] applicableTasks = extractApplicableTasks(annotationAttributes);

        // Extract and validate the target types this rule can process
        List<String> targetTypes = extractAndValidateTargetTypes(ruleClass, annotationAttributes);

        // Extract dependency information for DAG validation
        List<String> dependencyRules = extractDependencyRules(annotationAttributes, "dependsOn");
        List<String> predecessorRules = extractDependencyRules(annotationAttributes, "executeBefore");

        // Create metadata record containing all extracted information
        RuleMetadata metadata = new RuleMetadata(
                ruleClass.getQualifiedName().toString(),
                dependencyRules,
                predecessorRules
        );

        // Store metadata organized by task and target type for validation
        storeRuleMetadata(applicableTasks, targetTypes, metadata);
    }

    /**
     * Finds the {@code @Rule} annotation mirror for safe attribute access.
     *
     * <p>Annotation mirrors are used instead of direct annotation access to avoid
     * {@code MirroredTypesException} when the annotation contains {@code Class} values.
     * This exception occurs because the referenced classes might not be available
     * in the current compilation context.
     *
     * <p>The annotation mirror provides a safe way to access annotation attributes
     * as {@code TypeMirror} objects instead of {@code Class} objects.
     *
     * @param ruleClass the rule class to search for the annotation
     * @return the {@code @Rule} annotation mirror, or {@code null} if not found
     */
    private AnnotationMirror findRuleAnnotation(TypeElement ruleClass) {
        // Search through all annotation mirrors on the class
        return ruleClass.getAnnotationMirrors().stream()
                .filter(mirror -> {
                    // Match by fully qualified annotation type name
                    String annotationType = mirror.getAnnotationType().toString();
                    return annotationType.equals("edu.kit.datamanager.idoris.rules.logic.Rule");
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts all annotation attributes from a {@code @Rule} annotation mirror.
     *
     * <p>This method creates a convenient map of attribute names to their values,
     * making it easy to access specific annotation attributes in subsequent processing
     * steps.
     *
     * <p>The map keys are the simple names of the annotation methods (e.g., "appliesTo",
     * "dependsOn"), and the values are the corresponding {@code AnnotationValue} objects
     * that can be safely processed without triggering {@code MirroredTypesException}.
     *
     * @param ruleAnnotation the annotation mirror to extract attributes from
     * @return a map of attribute names to their annotation values
     */
    private Map<String, AnnotationValue> extractAnnotationAttributes(AnnotationMirror ruleAnnotation) {
        Map<String, AnnotationValue> attributes = new HashMap<>();

        // Iterate through all annotation attributes (method-value pairs)
        ruleAnnotation.getElementValues().forEach((method, value) -> {
            // Use the simple name of the method as the key (e.g., "appliesTo")
            String attributeName = method.getSimpleName().toString();
            attributes.put(attributeName, value);
        });

        return attributes;
    }

    /**
     * Extracts the tasks this rule applies to from the annotation attributes.
     *
     * <p>The {@code tasks} attribute specifies which rule tasks (VALIDATE, CONSUME, etc.)
     * this rule should be executed for. If not specified, the rule applies to all tasks.
     *
     * <p>This method handles:
     * <ul>
     *   <li>Missing tasks attribute (defaults to all tasks)</li>
     *   <li>Single task specification</li>
     *   <li>Multiple task specification</li>
     *   <li>Enum value extraction from string representations</li>
     * </ul>
     *
     * @param attributes the map of annotation attributes
     * @return array of {@code RuleTask} enums this rule applies to
     */
    private RuleTask[] extractApplicableTasks(Map<String, AnnotationValue> attributes) {
        AnnotationValue tasksAttribute = attributes.get("tasks");

        if (tasksAttribute == null) {
            // If tasks not specified, default to all available tasks
            return RuleTask.values();
        }

        // The tasks attribute contains an array of enum values
        @SuppressWarnings("unchecked")
        List<? extends AnnotationValue> taskValues = (List<? extends AnnotationValue>) tasksAttribute.getValue();

        // Convert each enum value to its corresponding RuleTask
        return taskValues.stream()
                .map(this::extractEnumName)  // Extract enum name from fully qualified string
                .map(RuleTask::valueOf)      // Convert to RuleTask enum
                .toArray(RuleTask[]::new);
    }

    /**
     * Extracts the simple enum name from its fully qualified string representation.
     *
     * <p>During annotation processing, enum values are represented as strings like
     * "com.example.EnumClass.ENUM_VALUE". This method extracts just the "ENUM_VALUE"
     * part for use with {@code Enum.valueOf()}.
     *
     * @param enumValue the annotation value containing the enum reference
     * @return the simple enum name (e.g., "VALIDATE" from "...RuleTask.VALIDATE")
     */
    private String extractEnumName(AnnotationValue enumValue) {
        String fullEnumString = enumValue.getValue().toString();
        // Extract everything after the last dot
        return fullEnumString.substring(fullEnumString.lastIndexOf('.') + 1);
    }

    /**
     * Extracts and validates the target types this rule can process.
     *
     * <p>This method processes the {@code appliesTo} attribute which specifies
     * which types of visitable elements this rule can handle. It performs
     * comprehensive validation to ensure type safety:
     *
     * <ul>
     *   <li><strong>Presence Check:</strong> Ensures the appliesTo attribute is specified</li>
     *   <li><strong>Type Validation:</strong> Validates each target type extends VisitableElement</li>
     *   <li><strong>Concreteness Check:</strong> Ensures target types are not abstract</li>
     * </ul>
     *
     * @param ruleClass  the rule class being processed (for error reporting)
     * @param attributes the map of annotation attributes
     * @return list of fully qualified target type names
     */
    private List<String> extractAndValidateTargetTypes(TypeElement ruleClass, Map<String, AnnotationValue> attributes) {
        AnnotationValue appliesToAttribute = attributes.get("appliesTo");

        if (appliesToAttribute == null) {
            // appliesTo is a required attribute
            reportError(ruleClass,
                    "Rule %s must specify the 'appliesTo' attribute. " +
                            "Add 'appliesTo = {YourTargetType.class}' to the @Rule annotation.",
                    ruleClass.getQualifiedName());
            return Collections.emptyList();
        }

        // The appliesTo attribute contains an array of Class references
        @SuppressWarnings("unchecked")
        List<? extends AnnotationValue> typeValues = (List<? extends AnnotationValue>) appliesToAttribute.getValue();

        // Process each target type: validate and convert to string
        return typeValues.stream()
                .map(typeValue -> (TypeMirror) typeValue.getValue())  // Get TypeMirror from AnnotationValue
                .peek(targetType -> validateTargetType(ruleClass, targetType))  // Validate each type
                .map(TypeMirror::toString)  // Convert to string representation
                .collect(Collectors.toList());
    }

    /**
     * Validates a single target type for compliance with rule requirements.
     *
     * <p>This method performs two critical validations:
     * <ol>
     *   <li><strong>Inheritance Check:</strong> Ensures the type extends VisitableElement</li>
     *   <li><strong>Concreteness Check:</strong> Ensures the type is not abstract</li>
     * </ol>
     *
     * <p>These validations prevent runtime errors and ensure that rules can only
     * be applied to valid, instantiable visitable elements.
     *
     * @param ruleClass  the rule class being processed (for error context)
     * @param targetType the target type to validate
     */
    private void validateTargetType(TypeElement ruleClass, TypeMirror targetType) {
        // Ensure the target type extends VisitableElement
        validateExtendsVisitableElement(ruleClass, targetType);

        // Ensure the target type is not abstract
        validateNotAbstractClass(ruleClass, targetType);
    }

    /**
     * Validates that a target type extends {@link VisitableElement}.
     *
     * <p>All rule target types must extend VisitableElement to be compatible
     * with the rule execution framework. This validation uses type assignability
     * checking to handle inheritance hierarchies correctly.
     *
     * @param ruleClass  the rule class being processed (for error context)
     * @param targetType the target type to validate
     */
    private void validateExtendsVisitableElement(TypeElement ruleClass, TypeMirror targetType) {
        // Look up the interface VisitableElement
        TypeElement visitableElement = elementUtilities.getTypeElement("edu.kit.datamanager.idoris.rules.logic.VisitableElement");

        if (visitableElement != null && !typeUtilities.isAssignable(targetType, visitableElement.asType())) {
            reportError(ruleClass,
                    "Rule %s targets type %s which does not extend VisitableElement. " +
                            "Ensure your target type extends VisitableElement or one of its subclasses.",
                    ruleClass.getQualifiedName(),
                    targetType);
        }
    }

    /**
     * Validates that a target type is not an abstract class.
     *
     * <p>Rules cannot be applied to abstract classes because abstract classes
     * cannot be instantiated. This validation prevents configuration errors
     * that would cause runtime failures.
     *
     * @param ruleClass  the rule class being processed (for error context)
     * @param targetType the target type to validate
     */
    private void validateNotAbstractClass(TypeElement ruleClass, TypeMirror targetType) {
        if (targetType instanceof DeclaredType declaredType) {
            Element typeElement = declaredType.asElement();

            // Check if the type element is a TypeElement with abstract modifier
            if (typeElement instanceof TypeElement te &&
                    te.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT)) {
                reportError(ruleClass,
                        "Rule %s cannot target abstract type %s. " +
                                "Rules can only be applied to concrete (non-abstract) classes.",
                        ruleClass.getQualifiedName(),
                        targetType);
            }
        }
    }

    /**
     * Extracts dependency rule class names from annotation attributes.
     *
     * <p>This method extracts dependency information from either the {@code dependsOn}
     * or {@code executeBefore} attributes. Both attributes contain arrays of Class
     * references that specify rule dependencies.
     *
     * <p>The extracted dependencies are used to build dependency graphs for
     * cycle detection and execution ordering.
     *
     * @param attributes    the map of annotation attributes
     * @param attributeName the name of the attribute to extract ("dependsOn" or "executeBefore")
     * @return list of fully qualified dependency rule class names
     */
    private List<String> extractDependencyRules(Map<String, AnnotationValue> attributes, String attributeName) {
        AnnotationValue dependencyAttribute = attributes.get(attributeName);

        if (dependencyAttribute == null) {
            // Dependency attributes are optional
            return Collections.emptyList();
        }

        // The dependency attribute contains an array of Class references
        @SuppressWarnings("unchecked")
        List<? extends AnnotationValue> dependencyValues = (List<? extends AnnotationValue>) dependencyAttribute.getValue();

        // Convert each Class reference to its fully qualified name
        return dependencyValues.stream()
                .map(dependency -> dependency.getValue().toString())
                .collect(Collectors.toList());
    }

    /**
     * Stores rule metadata in the organized collection structure.
     *
     * <p>This method stores the extracted rule metadata in the three-level nested
     * map structure that organizes rules by task and target type. This organization
     * enables independent validation of dependency graphs within each context.
     *
     * <p>For a rule that applies to multiple tasks or target types, the metadata
     * is stored in multiple locations to ensure proper validation in each context.
     *
     * @param applicableTasks the tasks this rule applies to
     * @param targetTypes     the target types this rule can process
     * @param metadata        the complete rule metadata to store
     */
    private void storeRuleMetadata(RuleTask[] applicableTasks, List<String> targetTypes, RuleMetadata metadata) {
        // Store the rule metadata for each task-target combination
        for (RuleTask task : applicableTasks) {
            for (String targetType : targetTypes) {
                // Navigate the three-level nested map structure
                rulesByTaskAndTarget
                        .computeIfAbsent(task, k -> new HashMap<>())           // Task level
                        .computeIfAbsent(targetType, k -> new HashMap<>())     // Target type level
                        .put(metadata.className(), metadata);                  // Rule level
            }
        }
    }

    /**
     * Validates all collected rule dependency graphs for cycles and consistency.
     *
     * <p>This method is called during the final processing round when all source
     * files have been processed and the complete dependency graphs are available.
     * It validates each (task, target-type) combination independently to detect
     * any circular dependencies that would cause infinite loops during execution.
     *
     * <p>The validation process:
     * <ol>
     *   <li>Iterates through each task type (VALIDATE, CONSUME, etc.)</li>
     *   <li>For each task, iterates through each target type</li>
     *   <li>Validates the dependency graph for that specific context</li>
     *   <li>Reports any cycles found as compilation errors</li>
     * </ol>
     */
    private void validateAllRuleDependencyGraphs() {
        // Validate each task-target combination independently
        rulesByTaskAndTarget.forEach((task, rulesByTarget) ->
                rulesByTarget.forEach((targetType, rulesInGraph) ->
                        validateDependencyGraph(task, targetType, rulesInGraph)));
    }

    /**
     * Validates a single dependency graph for cycles using topological sorting.
     *
     * <p>This method validates the dependency graph for a specific (task, target-type)
     * combination. It uses topological sorting with cycle detection to ensure that
     * the rules can be executed in a valid order without infinite loops.
     *
     * <p>If a cycle is detected, a compilation error is reported with details
     * about the problematic dependencies.
     *
     * @param task         the task context being validated
     * @param targetType   the target type context being validated
     * @param rulesInGraph the rules in this specific dependency graph
     */
    private void validateDependencyGraph(RuleTask task, String targetType, Map<String, RuleMetadata> rulesInGraph) {
        try {
            // Attempt to topologically sort the dependency graph
            performTopologicalSort(rulesInGraph);

            // If successful, the graph is acyclic and valid

        } catch (CyclicDependencyException exception) {
            // Cycle detected - report as compilation error
            reportError(null,
                    "Cyclic dependency detected in %s rules for target type %s: %s. " +
                            "Review the 'dependsOn' and 'executeBefore' attributes to eliminate the cycle.",
                    task,
                    targetType,
                    exception.getMessage());
        }
    }

    /**
     * Performs topological sort with cycle detection on a rule dependency graph.
     *
     * <p>This method implements Kahn's algorithm for topological sorting with
     * explicit cycle detection. It builds a directed graph from the rule dependencies
     * and attempts to sort the nodes topologically.
     *
     * <p>The algorithm:
     * <ol>
     *   <li>Builds an adjacency list representation of the dependency graph</li>
     *   <li>Performs depth-first search from each unvisited node</li>
     *   <li>Detects cycles by tracking node visit states</li>
     *   <li>Returns topologically sorted rules if no cycles exist</li>
     * </ol>
     *
     * @param rulesInGraph the rules to sort topologically
     * @return list of rules in topological order
     * @throws CyclicDependencyException if a cycle is detected in the dependency graph
     */
    private List<RuleMetadata> performTopologicalSort(Map<String, RuleMetadata> rulesInGraph) {
        // Build adjacency list representation of the dependency graph
        Map<String, List<String>> dependencyGraph = buildDependencyGraph(rulesInGraph);

        // Track visit states for cycle detection
        Map<String, VisitState> visitStates = new HashMap<>();

        // Collect topologically sorted rules
        List<RuleMetadata> sortedRules = new ArrayList<>();

        // Perform depth-first search from each unvisited node
        for (String ruleName : dependencyGraph.keySet()) {
            if (visitStates.get(ruleName) == null) {
                performDepthFirstSearch(ruleName, dependencyGraph, visitStates, sortedRules, rulesInGraph);
            }
        }

        return sortedRules;
    }

    /**
     * Builds a directed graph representation of rule dependencies.
     *
     * <p>This method constructs an adjacency list representation of the dependency
     * graph from the rule metadata. It handles both types of dependencies:
     * <ul>
     *   <li><strong>dependsOn:</strong> Current rule depends on other rules</li>
     *   <li><strong>executeBefore:</strong> Current rule must execute before other rules</li>
     * </ul>
     *
     * <p>The resulting graph has edges pointing from dependent rules to their dependencies,
     * allowing for standard topological sorting algorithms.
     *
     * @param rulesInGraph the rules to build a dependency graph for
     * @return adjacency list representation of the dependency graph
     */
    private Map<String, List<String>> buildDependencyGraph(Map<String, RuleMetadata> rulesInGraph) {
        Map<String, List<String>> dependencyGraph = new HashMap<>();

        // Initialize adjacency lists for all rules
        rulesInGraph.keySet().forEach(ruleName -> dependencyGraph.put(ruleName, new ArrayList<>()));

        // Build edges from rule dependencies
        for (RuleMetadata rule : rulesInGraph.values()) {
            String currentRule = rule.className();

            // Add edges for "dependsOn" relationships
            // Current rule depends on these rules (edges: current -> dependency)
            rule.dependsOn().stream()
                    .filter(dependencyGraph::containsKey)  // Only include dependencies in this graph
                    .forEach(dependency -> dependencyGraph.get(currentRule).add(dependency));

            // Add edges for "executeBefore" relationships
            // Current rule executes before these rules (edges: predecessor -> current)
            rule.executeBefore().stream()
                    .filter(dependencyGraph::containsKey)  // Only include rules in this graph
                    .forEach(predecessor -> dependencyGraph.get(predecessor).add(currentRule));
        }

        return dependencyGraph;
    }

    /**
     * Performs depth-first search for topological sorting with cycle detection.
     *
     * <p>This method implements the core DFS algorithm for topological sorting.
     * It uses a three-color scheme to detect cycles:
     * <ul>
     *   <li><strong>White (null):</strong> Unvisited node</li>
     *   <li><strong>Gray (VISITING):</strong> Currently being processed</li>
     *   <li><strong>Black (VISITED):</strong> Completely processed</li>
     * </ul>
     *
     * <p>A cycle is detected when we encounter a gray node during traversal,
     * indicating a back edge in the dependency graph.
     *
     * @param currentRule     the current rule being processed
     * @param dependencyGraph the adjacency list representation of dependencies
     * @param visitStates     the visit state tracking for cycle detection
     * @param sortedRules     the accumulator for topologically sorted rules
     * @param rulesInGraph    the complete rule metadata map
     * @throws CyclicDependencyException if a cycle is detected
     */
    private void performDepthFirstSearch(String currentRule,
                                         Map<String, List<String>> dependencyGraph,
                                         Map<String, VisitState> visitStates,
                                         List<RuleMetadata> sortedRules,
                                         Map<String, RuleMetadata> rulesInGraph) {
        // Mark current node as being visited (gray)
        visitStates.put(currentRule, VisitState.VISITING);

        // Process all dependencies of the current rule
        for (String dependencyRule : dependencyGraph.getOrDefault(currentRule, Collections.emptyList())) {
            VisitState dependencyState = visitStates.get(dependencyRule);

            if (dependencyState == VisitState.VISITING) {
                // Found a back edge - cycle detected!
                throw new CyclicDependencyException(
                        String.format("Cycle detected: %s depends on %s", currentRule, dependencyRule));
            }

            if (dependencyState == null) {
                // Unvisited node - recursively process it
                performDepthFirstSearch(dependencyRule, dependencyGraph, visitStates, sortedRules, rulesInGraph);
            }

            // If dependencyState == VISITED, we've already processed this node
        }

        // Mark current node as completely processed (black)
        visitStates.put(currentRule, VisitState.VISITED);

        // Add to sorted list (post-order traversal gives reverse topological order)
        sortedRules.add(rulesInGraph.get(currentRule));
    }

    /**
     * Reports a compilation error with formatted message.
     *
     * <p>This utility method provides a consistent way to report compilation errors
     * with proper formatting and optional element context. The errors are reported
     * through the compiler's messaging system and will cause the compilation to fail.
     *
     * @param element       the program element associated with the error (may be null)
     * @param messageFormat the format string for the error message
     * @param arguments     the arguments for the format string
     */
    private void reportError(Element element, String messageFormat, Object... arguments) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format(messageFormat, arguments),
                element
        );
    }

    /**
     * Enumeration representing the visit state of a node during depth-first search.
     *
     * <p>This three-color scheme is used for cycle detection in the dependency graph:
     * <ul>
     *   <li><strong>VISITING (Gray):</strong> Node is currently being processed</li>
     *   <li><strong>VISITED (Black):</strong> Node has been completely processed</li>
     *   <li><strong>Unvisited (White):</strong> Represented by null in the visit state map</li>
     * </ul>
     */
    private enum VisitState {
        /**
         * Node is currently being processed (gray in graph theory)
         */
        VISITING,
        /**
         * Node has been completely processed (black in graph theory)
         */
        VISITED
    }

    /**
     * Exception thrown when a cyclic dependency is detected in the rule dependency graph.
     *
     * <p>This exception is used internally during topological sorting to signal
     * that a cycle has been detected. It's caught and converted to a compilation
     * error for user-friendly reporting.
     */
    private static class CyclicDependencyException extends RuntimeException {
        /**
         * Constructs a new cyclic dependency exception with the specified detail message.
         *
         * @param message the detail message describing the cycle
         */
        public CyclicDependencyException(String message) {
            super(message);
        }
    }

    /**
     * Immutable record containing metadata about a rule class.
     *
     * <p>This record encapsulates all the information needed for dependency validation:
     * <ul>
     *   <li><strong>className:</strong> Fully qualified name of the rule class</li>
     *   <li><strong>dependsOn:</strong> List of rule classes this rule depends on</li>
     *   <li><strong>executeBefore:</strong> List of rule classes this rule must execute before</li>
     * </ul>
     *
     * <p>The record is used to build dependency graphs and perform topological sorting
     * for cycle detection.
     *
     * @param className     the fully qualified name of the rule class
     * @param dependsOn     list of fully qualified names of rules this rule depends on
     * @param executeBefore list of fully qualified names of rules this rule must execute before
     */
    private record RuleMetadata(
            String className,
            List<String> dependsOn,
            List<String> executeBefore
    ) {
    }
}