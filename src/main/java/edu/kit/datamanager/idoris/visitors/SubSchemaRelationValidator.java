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
import edu.kit.datamanager.idoris.domain.enums.SubSchemaRelation;
import edu.kit.datamanager.idoris.domain.relationships.AttributeReference;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import jakarta.validation.constraints.NotNull;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
public class SubSchemaRelationValidator implements Visitor<ValidationResult> {
    Map<String, ValidationResult> cache = new HashMap<>();

    @Override
    public ValidationResult visit(AttributeReference attributeReference, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attributeReference.getId())) != null) return result;

        log.info("Redirecting from AttributeReference " + attributeReference.getId() + " to Attribute " + attributeReference.getAttribute().getId());
        return save(attributeReference.getId(), attributeReference.getAttribute().execute(this, args));
    }

    @Override
    public ValidationResult visit(ProfileAttribute profileAttribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(profileAttribute.getId())) != null) return result;
        log.info("Redirecting from ProfileAttribute " + profileAttribute.getId() + " to DataType " + profileAttribute.getDataType().getPid());
        return save(profileAttribute.getId(), profileAttribute.getDataType().execute(this, args));
    }

    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getId())) != null) return result;
        log.info("Redirecting from Attribute " + attribute.getId() + " to DataType " + attribute.getDataType().getPid());
        return save(attribute.getId(), attribute.getDataType().execute(this, args));
    }

    @Override
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result = new ValidationResult();
        if (attributeMapping.getInput() == null && attributeMapping.getOutput() == null) return result;

        Attribute input = attributeMapping.getInput();
        Attribute output = attributeMapping.getOutput();
        if (input != null) result.addChild(input.execute(this, args));
        if (output != null) result.addChild(output.execute(this, args));
        return save(attributeMapping.getId(), result);
    }

    @Override
    public ValidationResult visit(BasicDataType basicDataType, Object... args) {
        return notAllowed(basicDataType);
    }

    @Override
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(typeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        if (typeProfile.getInheritsFrom().isEmpty()) {
            log.info("TypeProfile " + typeProfile.getPid() + " has no parent TypeProfiles. Skipping validation.");
            return result;
        }
        for (TypeProfile parent : typeProfile.getInheritsFrom()) {
            if (parent == null || parent.getSubSchemaRelation() == null) {
                log.info("Parent TypeProfile is null or has no SubSchemaRelation defined. Skipping validation.");
                continue;
            }
            log.info("Validating TypeProfile " + typeProfile.getPid() + " against parent TypeProfile " + parent.getPid());
            result.addChild(parent.execute(this, args));
            switch (parent.getSubSchemaRelation()) {
                case denyAdditionalProperties:
                    if (!typeProfile.getAttributes().isEmpty()) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " defines additional properties, but inherits from the TypeProfile " + parent.getPid() + " that denies additional properties.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("countOfAttributes", typeProfile.getAttributes().size())), ValidationResult.ValidationMessage.MessageType.ERROR);
                    }
                    break;
                case allowAdditionalProperties:
                    log.info("TypeProfile " + typeProfile.getPid() + " meets all requirements.");
                    break;
                case requireAllProperties:
                    for (ProfileAttribute attribute : parent.getAttributes()) {
                        List<ProfileAttribute> undefinedAttributes = typeProfile.getAttributes().stream().filter(a -> a.getDataType().equals(attribute.getDataType())).toList();
                        if (!undefinedAttributes.isEmpty()) {
                            result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define all properties defined in the TypeProfile " + parent.getPid() + " that requires all properties.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("numberOfUndefinedAttributes", undefinedAttributes.size(), "undefinedAttributes", undefinedAttributes)), ValidationResult.ValidationMessage.MessageType.ERROR);
                        }
                    }
                    break;
                case requireAnyOfProperties:
                    if (typeProfile.getAttributes().stream().noneMatch(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType())))) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define any property defined in the TypeProfile " + parent.getPid() + " that requires at least one property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), ValidationResult.ValidationMessage.MessageType.ERROR);
                    }
                    break;
                case requireOneOfProperties:
                    if (typeProfile.getAttributes().stream().filter(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType()))).count() != 1) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define exactly one property defined in the TypeProfile " + parent.getPid() + " that requires exactly one property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), ValidationResult.ValidationMessage.MessageType.ERROR);
                    }
                    break;
                case requireNoneOfProperties:
                    List<ProfileAttribute> illegallyDefinedAttributes = typeProfile.getAttributes().stream().filter(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType()))).toList();
                    if (!illegallyDefinedAttributes.isEmpty()) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " defines a property defined in the TypeProfile " + parent.getPid() + " that requires no property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("numberOfIllegallyDefinedAttributes", illegallyDefinedAttributes.size(), "illegallyDefinedAttributes", illegallyDefinedAttributes)), ValidationResult.ValidationMessage.MessageType.ERROR);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown SubSchemaRelation " + parent.getSubSchemaRelation());
            }
        }
        log.info("Validation of TypeProfile " + typeProfile.getPid() + " against parent TypeProfiles completed. result=" + result);
        return save(typeProfile.getPid(), result);
    }

    @Override
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operation.getPid())) != null) return result;
        else result = new ValidationResult();

        for (OperationStep step : operation.getExecution()) result.addChild(step.execute(this, args));
        AttributeReference executableOn = operation.getExecutableOn();
        if (executableOn != null) result.addChild(executableOn.execute(this, args));
        for (AttributeReference returns : operation.getReturns()) result.addChild(returns.execute(this, args));
        for (AttributeReference env : operation.getEnvironment()) result.addChild(env.execute(this, args));
        return save(operation.getPid(), result);
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();

        for (AttributeMapping input : operationStep.getAttributes()) result.addChild(input.execute(this, args));
        for (AttributeMapping output : operationStep.getOutput()) result.addChild(output.execute(this, args));
        for (OperationStep step : operationStep.getSteps()) result.addChild(step.execute(this, args));
        OperationTypeProfile otp = operationStep.getOperationTypeProfile();
        if (otp != null) result.addChild(otp.execute(this, args));
        Operation operation = operationStep.getOperation();
        if (operation != null) result.addChild(operation.execute(this, args));
        return save(operationStep.getId(), result);
    }

    @Override
    public ValidationResult visit(OperationTypeProfile operationTypeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationTypeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        for (OperationTypeProfile parent : operationTypeProfile.getInheritsFrom())
            result.addChild(parent.execute(this, args));
        for (AttributeReference attribute : operationTypeProfile.getAttributes())
            result.addChild(attribute.execute(this, args));
        for (AttributeReference outputs : operationTypeProfile.getOutputs())
            result.addChild(outputs.execute(this, args));
        return save(operationTypeProfile.getPid(), result);
    }

    private ValidationResult notAllowed(@NotNull VisitableElement element) {
        log.warning("Element of type " + element.getClass().getName() + " not allowed in SubSchemaRelationValidator. Ignoring...");
        return new ValidationResult();
    }

    private Object getTypeProfileAndParentElementaryInformation(TypeProfile typeProfile, TypeProfile parent, Map<String, Object> otherInformation) {
        Map<String, Object> result = new HashMap<>();
        result.put("this", new elementaryInformation(typeProfile.getPid(), typeProfile.getName(), typeProfile.getSubSchemaRelation()));
        result.put("parent", new elementaryInformation(parent.getPid(), parent.getName(), parent.getSubSchemaRelation()));
        result.put("otherInformation", otherInformation);
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

    private record elementaryInformation(String pid, String name, SubSchemaRelation subSchemaRelation) {
    }
}
