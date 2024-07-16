/*
 * Copyright (c) 2024 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.visitors;

import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.domain.entities.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.kit.datamanager.idoris.visitors.ValidationResult.ValidationMessage.MessageSeverity.ERROR;

@Log
public abstract class Visitor<T> {
    private final Set<String> visited = new HashSet<>();
    private final Map<String, ValidationResult> cache = new HashMap<>();

    public abstract T visit(Attribute attribute, Object... args);

    public abstract T visit(AttributeMapping attributeMapping, Object... args);

    public abstract T visit(BasicDataType basicDataType, Object... args);

    public abstract T visit(TypeProfile typeProfile, Object... args);

    public abstract T visit(Operation operation, Object... args);

    public abstract T visit(OperationStep operationStep, Object... args);

    public abstract T visit(OperationTypeProfile operationTypeProfile, Object... args);

    protected final ValidationResult checkCache(String id) {
        if (cache.containsKey(id)) {
            log.info("Cache hit for " + id);
            return cache.get(id);
        }
        log.info("Cache miss for " + id);
        if (visited.contains(id)) {
            log.info("Cycle detected for " + id);
            return handleCircle(id);
        } else {
            visited.add(id);
        }
        return null;
    }

    protected ValidationResult handleCircle(String id) {
        return new ValidationResult().addMessage("Cycle detected", id, ERROR);
    }

    protected ValidationResult notAllowed(@NotNull VisitableElement element) {
        log.warning("Element of type " + element.getClass().getSimpleName() + " not allowed in " + this.getClass().getSimpleName() + ". Ignoring...");
        return new ValidationResult();
    }

    protected final ValidationResult save(String id, ValidationResult result) {
        cache.put(id, result);
        return result;
    }
}
