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
import edu.kit.datamanager.idoris.domain.enums.ExecutionMode;
import edu.kit.datamanager.idoris.domain.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.validators.ValidationResult;
import lombok.extern.java.Log;

import java.util.Arrays;

import static edu.kit.datamanager.idoris.validators.ValidationMessage.MessageSeverity.*;

@Log
public class SyntaxValidator extends Visitor<ValidationResult> {
    @Override
    public ValidationResult visit(Attribute attribute, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attribute.getPid())) != null) return result;
        else result = new ValidationResult();

        if (attribute.getName() == null || attribute.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the attribute.", attribute, ERROR);
        }

        if (attribute.getDescription() == null || attribute.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the attribute.", attribute, WARNING);
        }

        if (attribute.getDataType() == null) {
            result.addMessage("You MUST provide a data type for the attribute.", attribute, ERROR);
        } else {
            result.addChild(attribute.getDataType().execute(this, args));
        }

        if (attribute.getLowerBoundCardinality() == null) {
            result.addMessage("You MUST provide a lower bound cardinality for the attribute.", attribute, ERROR);
        } else if (attribute.getLowerBoundCardinality() < 0) {
            result.addMessage("The lower bound cardinality of an attribute MUST be a positive number or zero.", attribute, ERROR);
        }

        if (attribute.getUpperBoundCardinality() != null && attribute.getUpperBoundCardinality() < 0) {
            result.addMessage("The upper bound cardinality of an attribute MUST be a positive number or zero.", attribute, ERROR);
        } else if (attribute.getUpperBoundCardinality() != null && attribute.getLowerBoundCardinality() != null && attribute.getUpperBoundCardinality() < attribute.getLowerBoundCardinality()) {
            result.addMessage("The upper bound cardinality of an attribute MUST be greater than or equal to the lower bound cardinality.", attribute, ERROR);
        } else if (attribute.getUpperBoundCardinality() == null) {
            result.addMessage("This attribute represents an unlimited number of values. This is not recommended, as it may lead to unexpected results in the future. Please consider setting an upper bound cardinality.", attribute, WARNING);
        }

        if (attribute.getOverride() != null) {
            result.addChild(attribute.getOverride().execute(this, args));
        }

        return save(attribute.getPid(), result);
    }

    @Override
    public ValidationResult visit(AttributeMapping attributeMapping, Object... args) {
        ValidationResult result;
        if ((result = checkCache(attributeMapping.getId())) != null) return result;
        else result = new ValidationResult();

        if (attributeMapping.getName() == null || attributeMapping.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a name for the attribute mapping.", attributeMapping, WARNING);
        }

        if (attributeMapping.getIndex() != null && attributeMapping.getIndex() < 0) {
            result.addMessage("The index is out of range. It has to be a positive number or zero.", attributeMapping, ERROR);
        }

        if (attributeMapping.getOutput() == null) {
            result.addMessage("Output MUST be specified.", attributeMapping, ERROR);
        }

        if (attributeMapping.getInput() == null && attributeMapping.getValue() == null) {
            result.addMessage("Input and value MUST NOT be unspecified at the same time.", attributeMapping, ERROR);
        }

        // Check that the output cardinalities are compatible with the input cardinalities.
        if (attributeMapping.getInput() != null && attributeMapping.getOutput() != null) {
            // If the input has an upper cardinality of more than one, the output has at most one or null, and no index is specified, return an error.
            if (attributeMapping.getInput().getLowerBoundCardinality() > 0 && attributeMapping.getInput().getUpperBoundCardinality() != null && attributeMapping.getInput().getUpperBoundCardinality() > 1 &&
                    (attributeMapping.getOutput().getLowerBoundCardinality() < 1 || (attributeMapping.getOutput().getUpperBoundCardinality() != null && attributeMapping.getOutput().getUpperBoundCardinality() < 1)) &&
                    attributeMapping.getIndex() == null) {
                result.addMessage("The output cardinality is not compatible with the input cardinality. If the input has an upper cardinality of more than one, the output must have at least a lower cardinality of one and an upper cardinality of one or null.", attributeMapping, ERROR);
            }

            // If the input has an upper cardinality smaller than the lower cardinality of the output, return an error.
            if (attributeMapping.getInput().getUpperBoundCardinality() != null && attributeMapping.getOutput().getLowerBoundCardinality() > attributeMapping.getInput().getUpperBoundCardinality()) {
                result.addMessage("The output cardinality is not compatible with the input cardinality. The lower bound cardinality of the output must be less than or equal to the upper bound cardinality of the input.", attributeMapping, ERROR);
            }
        }

        return save(attributeMapping.getId(), result);
    }

    @Override
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        ValidationResult result;
        if ((result = checkCache(atomicDataType.getPid())) != null) return result;
        else result = new ValidationResult();

        visitDataType(atomicDataType, result);

        if (atomicDataType.getInheritsFrom() != null)
            result.addChild(atomicDataType.getInheritsFrom().execute(this, args));

        if (atomicDataType.getPrimitiveDataType() == null) {
            result.addMessage("You MUST provide a primitive data type for the basic data type. Please select from: " + Arrays.toString(PrimitiveDataTypes.values()), atomicDataType, ERROR);
        }

        // Ensure that all permitted and forbidden values are of the same primitive data type
        if (atomicDataType.getPermittedValues() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (!atomicDataType.getPrimitiveDataType().isValueValid(value)) {
                    result.addMessage("The permitted value '" + value + "' is not valid for the primitive data type " + atomicDataType.getPrimitiveDataType() + ".", atomicDataType, ERROR);
                }
            }
        }

        // Ensure that no conflicts of permitted and forbidden values exist
        if (atomicDataType.getPermittedValues() != null && atomicDataType.getForbiddenValues() != null) {
            for (String value : atomicDataType.getPermittedValues()) {
                if (atomicDataType.getForbiddenValues().contains(value)) {
                    result.addMessage("The permitted values and forbidden values of the atomic data type must not overlap. The value '" + value + "' is both permitted and forbidden.", atomicDataType, ERROR);
                }
            }
        }

        return save(atomicDataType.getPid(), result);
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

        if (typeProfile.getValidationPolicy() == null) {
            result.addMessage("You MUST provide a validation policy for the type profile. Please select from: " + Arrays.toString(CombinationOptions.values()), typeProfile, ERROR);
        }

        return save(typeProfile.getPid(), result);
    }

    @Override
    public ValidationResult visit(Operation operation, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operation.getPid())) != null) return result;
        else result = new ValidationResult();

        if (operation.getName() == null || operation.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation.", operation, ERROR);
        }

        if (operation.getDescription() == null || operation.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation.", operation, WARNING);
        }

        if (operation.getExecutableOn() == null) {
            result.addMessage("You MUST specify an attribute on which the operation can be executed.", operation, ERROR);
        }

        if ((operation.getReturns() == null || operation.getReturns().isEmpty())) {
            result.addMessage("There are no return values provided for this Operation.", operation, INFO);
        }

        if (operation.getExecution() == null || operation.getExecution().isEmpty()) {
            result.addMessage("You MUST specify at least one execution step for a valid operation.", operation, ERROR);
        }

        operation.getExecution().stream().map(operationStep -> operationStep.execute(this, args)).forEach(result::addChild);

        return save(operation.getPid(), result);
    }

    @Override
    public ValidationResult visit(OperationStep operationStep, Object... args) {
        ValidationResult result;
        if ((result = checkCache(operationStep.getId())) != null) return result;
        else result = new ValidationResult();

        if (operationStep.getName() == null || operationStep.getName().isEmpty()) {
            result.addMessage("For better human readability, you SHOULD provide a name for the operation step.", operationStep, WARNING);
        }

        if (operationStep.getIndex() == null) {
            result.addMessage("Execution order index MUST be specified. If multiple Operation Steps for an Operation have the same index, the execution may happen in random order or be parallelized.", operationStep, ERROR);
        }

        if (operationStep.getMode() == null) {
            result.addMessage("An execution mode must be specified. Default is synchronous execution. Select from: " + Arrays.toString(ExecutionMode.values()), operationStep, ERROR);
        }

        if ((operationStep.getExecuteOperation() == null && operationStep.getUseTechnology() == null) || (operationStep.getExecuteOperation() != null && operationStep.getUseTechnology() != null)) {
            result.addMessage("You MUST specify either an operation or an operation type profile for the operation step. You can only specify exactly one!", operationStep, ERROR);
        }

        if (operationStep.getExecuteOperation() != null) {
            result.addChild(operationStep.getExecuteOperation().execute(this, args));
        } else if (operationStep.getUseTechnology() != null) {
            result.addChild(operationStep.getUseTechnology().execute(this, args));
        }

        if (operationStep.getInputMappings() != null) {
            operationStep.getInputMappings().stream().map(attributeMapping -> attributeMapping.execute(this, args)).forEach(result::addChild);
        }

        if (operationStep.getOutputMappings() != null) {
            operationStep.getOutputMappings().stream().map(attributeMapping -> attributeMapping.execute(this, args)).forEach(result::addChild);
        }

        return save(operationStep.getId(), result);
    }

    @Override
    public ValidationResult visit(TechnologyInterface technologyInterface, Object... args) {
        ValidationResult result;
        if ((result = checkCache(technologyInterface.getPid())) != null) return result;
        else result = new ValidationResult();

        if (technologyInterface.getName() == null || technologyInterface.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the operation type profile.", technologyInterface, ERROR);
        }

        if (technologyInterface.getDescription() == null || technologyInterface.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the operation type profile.", technologyInterface, WARNING);
        }

        if (technologyInterface.getAttributes() != null) {
            technologyInterface.getAttributes().stream().map(attribute -> attribute.execute(this, args)).forEach(result::addChild);
        }

        if (technologyInterface.getOutputs() != null) {
            technologyInterface.getOutputs().stream().map(attribute -> attribute.execute(this, args)).forEach(result::addChild);
        }

        if (technologyInterface.getAdapters() == null || technologyInterface.getAdapters().isEmpty()) {
            result.addMessage("You SHOULD specify at least one adapter for the technology interface.", technologyInterface, WARNING);
        }

        return save(technologyInterface.getPid(), result);
    }

    private void visitDataType(DataType dataType, ValidationResult result) {
        if (dataType.getType() == null) {
            result.addMessage("You MUST provide a type for the data type. Please select from: " + Arrays.toString(DataType.TYPES.values()), dataType, ERROR);
        }

        if (dataType.getName() == null || dataType.getName().isEmpty()) {
            result.addMessage("For better human readability and understanding, you MUST provide a name for the data type.", dataType, ERROR);
        }

        if (dataType.getDescription() == null || dataType.getDescription().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a description for the data type.", dataType, WARNING);
        }

        if (dataType.getExpectedUseCases() == null || dataType.getExpectedUseCases().isEmpty()) {
            result.addMessage("For better human readability and understanding, you SHOULD provide a list of expected uses for the data type.", dataType, WARNING);
        }
    }

    @Override
    protected ValidationResult handleCircle(String id) {
        return new ValidationResult().addMessage("Cycle detected", id, INFO);
    }
}