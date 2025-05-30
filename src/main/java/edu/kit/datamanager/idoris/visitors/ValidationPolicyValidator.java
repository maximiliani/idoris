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

package edu.kit.datamanager.idoris.visitors;

import edu.kit.datamanager.idoris.domain.entities.*;
import edu.kit.datamanager.idoris.domain.enums.CombinationOptions;
import edu.kit.datamanager.idoris.validators.ValidationResult;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.kit.datamanager.idoris.validators.ValidationMessage.MessageSeverity.ERROR;
import static edu.kit.datamanager.idoris.validators.ValidationMessage.MessageSeverity.WARNING;

@Log
public class ValidationPolicyValidator extends Visitor<ValidationResult> {

    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getPid())) != null) return result;
        log.info("Redirecting from Attribute " + attribute.getPid() + " to DataType");
        if (attribute.getDataType() != null) result = attribute.getDataType().execute(this, args);
        return save(attribute.getPid(), result);
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
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        return notAllowed(atomicDataType);
    }

    @Override
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(typeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        if (typeProfile.getInheritsFrom() == null || typeProfile.getInheritsFrom().isEmpty()) {
            log.info("TypeProfile " + typeProfile.getPid() + " has no parent TypeProfiles. Skipping validation.");
            return save(typeProfile.getPid(), result);
        }
        for (TypeProfile parent : typeProfile.getInheritsFrom()) {
            if (parent == null || parent.getValidationPolicy() == null) {
                log.info("Parent TypeProfile is null or has no SubSchemaRelation defined. Skipping validation.");
                continue;
            }
            log.info("Validating TypeProfile " + typeProfile.getPid() + " against parent TypeProfile " + parent.getPid());
            result.addChild(parent.execute(this, args));

            if (!parent.isAllowAdditionalAttributes() && !typeProfile.getAttributes().isEmpty())
                result.addMessage("TypeProfile " + typeProfile.getPid() + " defines additional properties, but inherits from the TypeProfile " + parent.getPid() + " that denies additional properties.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("countOfAttributes", typeProfile.getAttributes().size())), ERROR);

            switch (parent.getValidationPolicy()) {
                case ALL -> {
                    for (Attribute attribute : parent.getAttributes()) {
                        List<Attribute> undefinedAttributes = typeProfile.getAttributes().stream().filter(a -> a.getDataType().equals(attribute.getDataType())).toList();
                        if (!undefinedAttributes.isEmpty()) {
                            result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define all properties defined in the TypeProfile " + parent.getPid() + " that requires all properties.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("numberOfUndefinedAttributes", undefinedAttributes.size(), "undefinedAttributes", undefinedAttributes)), ERROR);
                        }
                    }
                }
                case ANY -> {
                    if (typeProfile.getAttributes().stream().noneMatch(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType())))) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define any property defined in the TypeProfile " + parent.getPid() + " that requires at least one property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), ERROR);
                    }
                }
                case ONE -> {
                    if (typeProfile.getAttributes().stream().filter(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType()))).count() != 1) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " does not define exactly one property defined in the TypeProfile " + parent.getPid() + " that requires exactly one property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, null), ERROR);
                    }
                }
                case NONE -> {
                    List<Attribute> illegallyDefinedAttributes = typeProfile.getAttributes().stream().filter(a -> parent.getAttributes().stream().anyMatch(pa -> pa.getDataType().equals(a.getDataType()))).toList();
                    if (!illegallyDefinedAttributes.isEmpty()) {
                        result.addMessage("TypeProfile " + typeProfile.getPid() + " defines a property defined in the TypeProfile " + parent.getPid() + " that requires no property.", getTypeProfileAndParentElementaryInformation(typeProfile, parent, Map.of("numberOfIllegallyDefinedAttributes", illegallyDefinedAttributes.size(), "illegallyDefinedAttributes", illegallyDefinedAttributes)), ERROR);
                    }
                }
                default -> throw new IllegalStateException("Unknown ValidationPolicy " + parent.getValidationPolicy());
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

        if (operation.getExecution() != null)
            for (OperationStep step : operation.getExecution())
                result.addChild(step.execute(this, args));

        if (operation.getExecutableOn() != null)
            result.addChild(operation.getExecutableOn().execute(this, args));

        if (operation.getReturns() != null)
            for (Attribute returns : operation.getReturns())
                result.addChild(returns.execute(this, args));

        if (operation.getEnvironment() != null)
            for (Attribute env : operation.getEnvironment())
                result.addChild(env.execute(this, args));

        return save(operation.getPid(), result);
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();

        if (operationStep.getInputMappings() != null)
            for (AttributeMapping input : operationStep.getInputMappings())
                result.addChild(input.execute(this, args));

        if (operationStep.getOutputMappings() != null)
            for (AttributeMapping output : operationStep.getOutputMappings())
                result.addChild(output.execute(this, args));

        if (operationStep.getSubSteps() != null)
            for (OperationStep step : operationStep.getSubSteps())
                result.addChild(step.execute(this, args));

        if (operationStep.getUseTechnology() != null)
            result.addChild(operationStep.getUseTechnology().execute(this, args));

        if (operationStep.getExecuteOperation() != null)
            result.addChild(operationStep.getExecuteOperation().execute(this, args));

        return save(operationStep.getId(), result);
    }

    @Override
    public ValidationResult visit(TechnologyInterface technologyInterface, Object... args) {
        ValidationResult result;
        if ((result = checkCache(technologyInterface.getPid())) != null) return result;
        else result = new ValidationResult();

        if (technologyInterface.getAttributes() != null)
            for (Attribute attribute : technologyInterface.getAttributes())
                result.addChild(attribute.execute(this, args));

        if (technologyInterface.getOutputs() != null)
            for (Attribute outputs : technologyInterface.getOutputs())
                result.addChild(outputs.execute(this, args));

        return save(technologyInterface.getPid(), result);
    }

    private Object getTypeProfileAndParentElementaryInformation(TypeProfile typeProfile, TypeProfile parent, Map<String, Object> otherInformation) {
        Map<String, Object> result = new HashMap<>();
        result.put("this", new elementaryInformation(typeProfile.getPid(), typeProfile.getName(), typeProfile.getValidationPolicy()));
        result.put("parent", new elementaryInformation(parent.getPid(), parent.getName(), parent.getValidationPolicy()));
        result.put("otherInformation", otherInformation);
        return result;
    }

    @Override
    protected ValidationResult handleCircle(String id) {
        return new ValidationResult().addMessage("Cycle detected", id, WARNING);
    }

    private record elementaryInformation(String pid, String name, CombinationOptions validationPolicy) {
    }
}
