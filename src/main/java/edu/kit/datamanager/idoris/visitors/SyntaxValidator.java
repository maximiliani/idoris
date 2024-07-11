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
import edu.kit.datamanager.idoris.domain.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.domain.enums.SubSchemaRelation;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.Objects;

import static edu.kit.datamanager.idoris.visitors.ValidationResult.ValidationMessage.MessageType.ERROR;
import static edu.kit.datamanager.idoris.visitors.ValidationResult.ValidationMessage.MessageType.WARNING;

@Log
public class SyntaxValidator extends Visitor<ValidationResult> {
    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getPid())) != null) return result;
        else result = new ValidationResult();

        if (attribute.getName() == null || attribute.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the attribute.", ERROR);
        }

        if (attribute.getDescription() == null || attribute.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the attribute.", WARNING);
        }

        if (attribute.getDataType() == null) {
            result.addMessage("You MUST provide a data type for the attribute.", ERROR);
        }

        if (attribute.getObligation() == null) {
            result.addMessage("You MUST provide an obligation for the attribute. The default value is 'Mandatory'.", ERROR);
        }

        if (attribute.getOverride() != null) {
            result.addChild(attribute.getOverride().execute(this, args));
        }

        return result;
    }

    @Override
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attributeMapping.getId())) != null) return result;
        else result = new ValidationResult();

        if (attributeMapping.getName() == null || attributeMapping.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a name for the attribute mapping.", ValidationResult.ValidationMessage.MessageType.WARNING);
        }

        if (attributeMapping.getIndex() < 0) {
            result.addMessage("The index is out of range. It has to be a positive number or zero.", ERROR);
        }

        if (attributeMapping.getOutput() == null) {
            result.addMessage("Output MUST be specified.", ERROR);
        }

        if (attributeMapping.getInput() == null && attributeMapping.getValue() == null) {
            result.addMessage("Input and value MUST NOT be unspecified at the same time.", ERROR);
        }

        if (Objects.requireNonNull(attributeMapping.getInput()).isRepeatable() && !attributeMapping.getOutput().isRepeatable() && attributeMapping.getIndex() == null) {
            result.addMessage("The input is repeatable, the output is not repeatable and no index is specified.", ERROR);
        }

        return result;
    }

    @Override
    public ValidationResult visit(BasicDataType basicDataType, Object... args) {
        ValidationResult result;
        if ((result = checkCache(basicDataType.getPid())) != null) return result;
        else result = new ValidationResult();

        visitDataType(basicDataType, result);

        if (basicDataType.getInheritsFrom() != null)
            result.addChild(basicDataType.getInheritsFrom().execute(this, args));

        if (basicDataType.getPrimitiveDataType() == null) {
            result.addMessage("You MUST provide a primitive data type for the basic data type. Please select from: " + Arrays.toString(PrimitiveDataTypes.values()), ERROR);
        }

        if (basicDataType.getCategory() == null) {
            result.addMessage("You MUST provide a category for the basic data type. Please select from: " + Arrays.toString(BasicDataType.Category.values()), ERROR);
        } else switch (basicDataType.getCategory()) {
            case MeasurementUnit -> {
                if (basicDataType.getUnitName() == null || basicDataType.getUnitName().isEmpty()) {
                    result.addMessage("A measurement unit MUST have a unit name", ERROR);
                }
                if (basicDataType.getUnitSymbol() == null || basicDataType.getUnitSymbol().isEmpty()) {
                    result.addMessage("A measurement unit MUST have a unit symbol", ERROR);
                }
                if (basicDataType.getDefinedBy() == null || basicDataType.getDefinedBy().isEmpty()) {
                    result.addMessage("A measurement unit SHOULD specify the defined by field", ValidationResult.ValidationMessage.MessageType.WARNING);
                }
                if (basicDataType.getStandard_uncertainty() == null || basicDataType.getStandard_uncertainty().isEmpty()) {
                    result.addMessage("A measurement unit SHOULD specify the standard uncertainty", ValidationResult.ValidationMessage.MessageType.WARNING);
                }
            }
            case Format -> {
                if (basicDataType.getRegex() == null || basicDataType.getRegex().isEmpty()) {
                    result.addMessage("A format data type MUST have a regex", ERROR);
                }
                if (basicDataType.getRegexFlavour() == null || basicDataType.getRegexFlavour().isEmpty()) {
                    result.addMessage("A format data type MUST specify the regex flavour. The encouraged default value is 'ecma-262-RegExp'.'", ERROR);
                }
            }
            case Enumeration -> {
                if (basicDataType.getValueEnum() == null || basicDataType.getValueEnum().isEmpty()) {
                    result.addMessage("An enumerated BasicDataType MUST have provide a set of acceptable values", ERROR);
                }
            }
            default -> {
            }
        }

        return result;
    }

    @Override
    public ValidationResult visit(TypeProfile typeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(typeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        visitDataType(typeProfile, result);

        if (typeProfile.getInheritsFrom() != null)
            typeProfile.getInheritsFrom().stream().map(typeProfile1 -> typeProfile1.execute(this, args)).forEach(result::addChild);

        if (typeProfile.getAttributes() != null) {
            typeProfile.getAttributes().stream().map(attribute -> attribute.execute(this, args)).forEach(result::addChild);
        }

        if (typeProfile.getSubSchemaRelation() == null) {
            result.addMessage("You MUST provide a sub schema relation for the type profile. Please select from: " + Arrays.toString(SubSchemaRelation.values()), ERROR);
        }

        return result;
    }

    @Override
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operation.getPid())) != null) return result;
        else result = new ValidationResult();

        if (operation.getName() == null || operation.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation.", ERROR);
        }

        if (operation.getDescription() == null || operation.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation.", ValidationResult.ValidationMessage.MessageType.WARNING);
        }

        if (operation.getExecutableOn() == null) {
            result.addMessage("You MUST specify an attribute on which the operation can be executed.", ERROR);
        }

        if ((operation.getReturns() == null || operation.getReturns().isEmpty())) {
            result.addMessage("There are no return values provided for this Operation.", ValidationResult.ValidationMessage.MessageType.INFO);
        }

        if (operation.getExecution() == null || operation.getExecution().isEmpty()) {
            result.addMessage("You MUST specify at least one execution step for a valid operation.", ERROR);
        }

        operation.getExecution().stream().map(operationStep -> operationStep.execute(this, args)).forEach(result::addChild);

        return result;
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();

        if (operationStep.getName() == null || operationStep.getName().isEmpty()) {
            result.addMessage("For better human readability, you SHOULD provide a name for the operation step.", WARNING);
        }

        if (operationStep.getExecutionOrderIndex() == null) {
            result.addMessage("Execution order index MUST be specified. If multiple Operation Steps for an Operation have the same index, the execution may happen in random order or be parallelized.", ERROR);
        }

        if (operationStep.getMode() == null) {
            result.addMessage("An execution mode must be specified. Default is synchronous execution. Select from: " + Arrays.toString(OperationStep.ExecutionMode.values()), ERROR);
        }

        if ((operationStep.getOperation() == null && operationStep.getOperationTypeProfile() == null) || (operationStep.getOperation() != null && operationStep.getOperationTypeProfile() != null)) {
            result.addMessage("You MUST specify either an operation or an operation type profile for the operation step. You can only specify exactly one!", ERROR);
        }

        if (operationStep.getOperation() != null) {
            result.addChild(operationStep.getOperation().execute(this, args));
        } else if (operationStep.getOperationTypeProfile() != null) {
            result.addChild(operationStep.getOperationTypeProfile().execute(this, args));
        }

        if (operationStep.getAttributes() != null) {
            operationStep.getAttributes().stream().map(attributeMapping -> attributeMapping.execute(this, args)).forEach(result::addChild);
        }

        if (operationStep.getOutput() != null) {
            operationStep.getOutput().stream().map(attributeMapping -> attributeMapping.execute(this, args)).forEach(result::addChild);
        }

        return result;
    }

    @Override
    public ValidationResult visit(OperationTypeProfile operationTypeProfile, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationTypeProfile.getPid())) != null) return result;
        else result = new ValidationResult();

        if (operationTypeProfile.getName() == null || operationTypeProfile.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation type profile.", ERROR);
        }

        if (operationTypeProfile.getDescription() == null || operationTypeProfile.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation type profile.", ValidationResult.ValidationMessage.MessageType.WARNING);
        }

        if (operationTypeProfile.getInheritsFrom() != null) {
            operationTypeProfile.getInheritsFrom().stream().map(operationTypeProfile1 -> operationTypeProfile1.execute(this, args)).forEach(result::addChild);
        }

        if (operationTypeProfile.getAttributes() != null) {
            operationTypeProfile.getAttributes().stream().map(attribute -> attribute.execute(this, args)).forEach(result::addChild);
        }

        if (operationTypeProfile.getOutputs() != null) {
            operationTypeProfile.getOutputs().stream().map(attribute -> attribute.execute(this, args)).forEach(result::addChild);
        }

//        TODO
//        if (operationTypeProfile.getAdapters() != null) {
//            operationTypeProfile.getAdapters().stream().map(fdo -> fdo.execute(this, args)).forEach(result::addChild);
//        }

        return result;
    }

    private void visitDataType(DataType dataType, ValidationResult result) {
        if (dataType.getType() == null) {
            result.addMessage("You MUST provide a type for the data type. Please select from: " + Arrays.toString(DataType.TYPES.values()), ERROR);
        }

        if (dataType.getName() == null || dataType.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the data type.", ERROR);
        }

        if (dataType.getDescription() == null || dataType.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the data type.", ValidationResult.ValidationMessage.MessageType.WARNING);
        }

        if (dataType.getExpectedUses() == null || dataType.getExpectedUses().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a list of expected uses for the data type.", ValidationResult.ValidationMessage.MessageType.WARNING);
        }
    }
}