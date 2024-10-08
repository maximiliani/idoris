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
import edu.kit.datamanager.idoris.domain.enums.Obligation;
import edu.kit.datamanager.idoris.validators.ValidationResult;

import static edu.kit.datamanager.idoris.validators.ValidationMessage.MessageSeverity.ERROR;

public class InheritanceValidator extends Visitor<ValidationResult> {
    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getPid())) != null) return result;
        else result = new ValidationResult();

        if (attribute.getDataType() != null) result.addChild(attribute.getDataType().execute(this, args));
        if (attribute.getOverride() != null) result.addChild(attribute.getOverride().execute(this, args));

        if (attribute.getOverride() != null && attribute.getOverride().getDataType() != null) {
            Attribute override = attribute.getOverride();

            if (!attribute.getDataType().inheritsFrom(override.getDataType()))
                result.addMessage("The data type of an attribute MUST be inherited from the data type of the attribute that was overwritten.", attribute, ERROR);

            if (attribute.getObligation() == Obligation.Optional && override.getObligation() == Obligation.Mandatory)
                result.addMessage("The obligation of an attribute MUST be more or equally restrictive than the obligation of the attribute that was overwritten. Overriding a mandatory attribute as an optional attribute is NOT possible.", attribute, ERROR);
        }

        return save(attribute.getPid(), result);
    }

    @Override
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attributeMapping.getId())) != null) return result;
        else result = new ValidationResult();

        if (attributeMapping.getInput() != null) result.addChild(attributeMapping.getInput().execute(this, args));
        if (attributeMapping.getOutput() != null) result.addChild(attributeMapping.getOutput().execute(this, args));

        return save(attributeMapping.getId(), result);
    }

    @Override
    public ValidationResult visit(BasicDataType basicDataType, Object... args) {
        ValidationResult result;
        if ((result = checkCache(basicDataType.getPid())) != null) return result;
        else result = new ValidationResult();

        BasicDataType parent = basicDataType.getInheritsFrom();
        if (parent != null) {
            result.addChild(parent.execute(this, args));
            if (!basicDataType.getPrimitiveDataType().equals(parent.getPrimitiveDataType()))
                result.addMessage("Primitive data type does not match parent", basicDataType, ERROR);
            if (!basicDataType.getCategory().equals(parent.getCategory()))
                result.addMessage("Category does not match parent", basicDataType, ERROR);

            if ((parent.getValueEnum() != null && basicDataType.getValueEnum() != null) && !parent.getValueEnum().isEmpty() && !parent.getValueEnum().containsAll(basicDataType.getValueEnum()))
                result.addMessage("Value enum does not match parent", basicDataType, ERROR);
        }

        return save(basicDataType.getPid(), result);
    }

    @Override
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(typeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        if (typeProfile.getInheritsFrom() != null) {
            for (TypeProfile parent : typeProfile.getInheritsFrom()) {
                result.addChild(parent.execute(this));
            }
        }

        if (typeProfile.getAttributes() != null) {
            for (Attribute attribute : typeProfile.getAttributes()) {
                result.addChild(attribute.execute(this));
            }
        }

        return save(typeProfile.getPid(), result);
    }

    @Override
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operation.getPid())) != null) return result;
        else result = new ValidationResult();

        if (operation.getExecutableOn() != null) result.addChild(operation.getExecutableOn().execute(this, args));

        if (operation.getReturns() != null) {
            for (Attribute attribute : operation.getReturns()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        if (operation.getEnvironment() != null) {
            for (Attribute attribute : operation.getEnvironment()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        if (operation.getExecution() != null) {
            for (OperationStep step : operation.getExecution()) {
                result.addChild(step.execute(this, args));
            }
        }

        return save(operation.getPid(), result);
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();

        if (operationStep.getOperation() != null) result.addChild(operationStep.getOperation().execute(this, args));
        if (operationStep.getOperationTypeProfile() != null)
            result.addChild(operationStep.getOperationTypeProfile().execute(this, args));

        if (operationStep.getAttributes() != null) {
            for (AttributeMapping attributeMapping : operationStep.getAttributes()) {
                result.addChild(attributeMapping.execute(this, args));
            }
        }

        if (operationStep.getOutput() != null) {
            for (AttributeMapping attributeMapping : operationStep.getOutput()) {
                result.addChild(attributeMapping.execute(this, args));
            }
        }

        if (operationStep.getSteps() != null) {
            for (OperationStep child : operationStep.getSteps()) {
                result.addChild(child.execute(this, args));
            }
        }

        return save(operationStep.getId(), result);
    }

    @Override
    public ValidationResult visit(OperationTypeProfile operationTypeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationTypeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        if (operationTypeProfile.getAttributes() != null) {
            for (Attribute attribute : operationTypeProfile.getAttributes()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        if (operationTypeProfile.getInheritsFrom() != null) {
            for (OperationTypeProfile parent : operationTypeProfile.getInheritsFrom()) {
                result.addChild(parent.execute(this, args));
            }
        }

        if (operationTypeProfile.getOutputs() != null) {
            for (Attribute attribute : operationTypeProfile.getOutputs()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        return save(operationTypeProfile.getPid(), result);
    }
}
