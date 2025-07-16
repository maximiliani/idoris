/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.operations.entities.AttributeMapping;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import edu.kit.datamanager.idoris.operations.entities.OperationStep;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Abstract base class for visitors that process various types of visitable elements.
 * This class implements the Visitor pattern to navigate and process elements in the domain model.
 *
 * <p>The Visitor pattern allows for adding operations to a class hierarchy without modifying the classes themselves.
 * Each subclass can implement specific behavior for different element types.</p>
 *
 * <p>This implementation includes:</p>
 * <ul>
 *   <li>Cycle detection to prevent infinite recursion</li>
 *   <li>Result caching to avoid redundant processing</li>
 *   <li>A factory-based approach for creating output instances</li>
 * </ul>
 *
 * @param <T> the type of rule output produced by this visitor, must extend RuleOutput<T>
 */
@Slf4j
public abstract class Visitor<T extends RuleOutput<T>> {
    /**
     * Set to track visited element IDs to detect cycles in the visitation graph
     */
    private final Set<String> visited = new HashSet<>();

    /**
     * Cache for storing already processed results by element ID
     */
    private final Map<String, T> cache = new HashMap<>();

    /**
     * Factory for creating new output instances
     */
    private final Supplier<T> outputFactory;

    /**
     * Creates a new visitor with the specified output factory.
     *
     * @param outputFactory a supplier function that creates new instances of the output type T
     */
    protected Visitor(Supplier<T> outputFactory) {
        this.outputFactory = outputFactory;
    }

    /**
     * Visits an Attribute element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param attribute the attribute to visit
     * @param args      additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(Attribute attribute, Object... args) {
        return notAllowed(attribute);
    }


    /**
     * Visits an AttributeMapping element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param attributeMapping the attribute mapping to visit
     * @param args             additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(AttributeMapping attributeMapping, Object... args) {
        return notAllowed(attributeMapping);
    }

    /**
     * Visits an AtomicDataType element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param atomicDataType the atomic data type to visit
     * @param args           additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(AtomicDataType atomicDataType, Object... args) {
        return notAllowed(atomicDataType);
    }

    /**
     * Visits a TypeProfile element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param typeProfile the type profile to visit
     * @param args        additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(TypeProfile typeProfile, Object... args) {
        return notAllowed(typeProfile);
    }

    /**
     * Visits an Operation element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param operation the operation to visit
     * @param args      additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(Operation operation, Object... args) {
        return notAllowed(operation);
    }

    /**
     * Visits an OperationStep element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param operationStep the operation step to visit
     * @param args          additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(OperationStep operationStep, Object... args) {
        return notAllowed(operationStep);
    }

    /**
     * Visits a TechnologyInterface element and processes it.
     * Default implementation treats this as a not allowed element type.
     *
     * @param technologyInterface the technology interface to visit
     * @param args                additional arguments that may be used during visitation
     * @return processing result of type T
     */
    public T visit(TechnologyInterface technologyInterface, Object... args) {
        return notAllowed(technologyInterface);
    }

    /**
     * Checks if an element with the given ID has already been processed (cached).
     * This method also handles cycle detection during traversal.
     *
     * <p>The method performs the following steps:</p>
     * <ol>
     *   <li>Check if the element is in the cache, and return the cached result if found</li>
     *   <li>Check if the element has been visited before (cycle detection)</li>
     *   <li>If a cycle is detected, handle it with {@link #handleCircle(String)}</li>
     *   <li>If not visited before, mark it as visited</li>
     * </ol>
     *
     * @param id the unique identifier of the element being visited
     * @return the cached result if available, the result of cycle handling if a cycle is detected, or null otherwise
     */
    protected final T checkCache(String id) {
        if (cache.containsKey(id)) {
            log.debug("Cache hit for {}", id);
            return cache.get(id);
        }
        log.debug("Cache miss for {}", id);
        if (visited.contains(id)) {
            log.debug("Cycle detected for {}", id);
            if (id == null) return null;
            return handleCircle(id);
        } else {
            visited.add(id);
        }
        return null;
    }

    /**
     * Handles the detection of cycles in the visitation graph.
     * Creates a new output instance with an error message about the cycle.
     *
     * @param id the ID of the element where a cycle was detected
     * @return a new output instance with an error message about the cycle
     */
    protected T handleCircle(String id) {
        return outputFactory.get().addMessage("Cycle detected", OutputMessage.MessageSeverity.ERROR, id);
    }

    /**
     * Handles an element of a type that is not supported by this visitor.
     * Logs a warning and returns an empty output instance.
     *
     * @param element the element that is not allowed to be processed by this visitor
     * @return an empty output instance
     */
    protected T notAllowed(@NotNull VisitableElement element) {
        log.warn("Element of type {} not allowed in {}. Ignoring...", element.getClass().getSimpleName(), this.getClass().getSimpleName());
        return outputFactory.get();
    }

    /**
     * Saves the processing result for an element to the cache.
     * This prevents redundant processing if the same element is visited again.
     *
     * @param id     the unique identifier of the element
     * @param result the processing result to cache
     * @return the same result that was cached (for method chaining)
     */
    protected final T save(String id, T result) {
        cache.put(id, result);
        return result;
    }
}
