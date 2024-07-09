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

import edu.kit.datamanager.idoris.domain.entities.*;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.Map;

@Log
public class SyntaxValidator implements Visitor<ValidationResult> {
    private final Map<String, ValidationResult> cache = new HashMap<>();

    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getPid())) != null) return result;
        else result = new ValidationResult();

        return result;
    }

    @Override
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attributeMapping.getId())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    @Override
    public ValidationResult visit(BasicDataType basicDataType, Object... args) {
        ValidationResult result;
        if ((result = checkCache(basicDataType.getPid())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    @Override
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(typeProfile.getPid())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    @Override
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operation.getPid())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    @Override
    public ValidationResult visit(OperationTypeProfile operationTypeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationTypeProfile.getPid())) != null) return result;
        else result = new ValidationResult();
        return result;
    }

    private ValidationResult checkCache(String id) {
        if (cache.containsKey(id)) {
            log.info("Cache hit for TypeProfile " + id);
            return cache.get(id);
        }
        log.info("Cache miss for TypeProfile " + id);
        return null;
    }

    private ValidationResult save(String id, ValidationResult result) {
        cache.put(id, result);
        return result;
    }
}
