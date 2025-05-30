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

            if (attribute.getLowerBoundCardinality() < override.getLowerBoundCardinality())
                result.addMessage("The lower bound cardinality of an attribute MUST be more or equally restrictive than the lower bound cardinality of the attribute that was overwritten. Overriding a more restrictive attribute as a less restrictive attribute is NOT possible.", attribute, ERROR);
            if (attribute.getUpperBoundCardinality() == null && override.getUpperBoundCardinality() != null) {
                result.addMessage("The upper bound cardinality of an attribute MUST be defined if the attribute that was overwritten has an upper bound cardinality defined.", attribute, ERROR);
            } else if (override.getUpperBoundCardinality() != null && attribute.getUpperBoundCardinality() > override.getUpperBoundCardinality())
                result.addMessage("The upper bound cardinality of an attribute MUST be more or equally restrictive than the upper bound cardinality of the attribute that was overwritten. Overriding a less restrictive attribute as a more restrictive attribute is NOT possible.", attribute, ERROR);
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
    public ValidationResult visit(AtomicDataType atomicDataType, Object... args) {
        ValidationResult result;
        if ((result = checkCache(atomicDataType.getPid())) != null) return result;
        else result = new ValidationResult();

        AtomicDataType parent = atomicDataType.getInheritsFrom();
        if (parent != null) {
            result.addChild(parent.execute(this, args));
            if (!atomicDataType.getPrimitiveDataType().equals(parent.getPrimitiveDataType()))
                result.addMessage("Primitive data type does not match parent", atomicDataType, ERROR);

            // Compare permitted values with parent
            if (parent.getPermittedValues() != null && parent.getPermittedValues().size() > 0) {
                if (atomicDataType.getPermittedValues() == null || atomicDataType.getPermittedValues().isEmpty())
                    result.addMessage("Permitted values are not defined for atomic data type, but should contain at least those defined by the parent", atomicDataType, ERROR);
                else if (!atomicDataType.getPermittedValues().containsAll(parent.getPermittedValues()))
                    result.addMessage("Permitted values do not match parent", atomicDataType, ERROR);
            }

            // Compare forbidden values with parent
            if (parent.getForbiddenValues() != null && parent.getForbiddenValues().size() > 0) {
                if (atomicDataType.getForbiddenValues() == null || atomicDataType.getForbiddenValues().isEmpty())
                    result.addMessage("Forbidden values are not defined for atomic data type, but should contain at least those defined by the parent", atomicDataType, ERROR);
                else if (!atomicDataType.getForbiddenValues().containsAll(parent.getForbiddenValues()))
                    result.addMessage("Forbidden values do not match parent", atomicDataType, ERROR);
            }

            // Detect conflicts between permitted and forbidden values in atomic data type and parent
            if (atomicDataType.getPermittedValues() != null && atomicDataType.getForbiddenValues() != null &&
                    !atomicDataType.getPermittedValues().isEmpty() && !atomicDataType.getForbiddenValues().isEmpty() &&
                    atomicDataType.getPermittedValues().stream().anyMatch(atomicDataType.getForbiddenValues()::contains)) {
                result.addMessage("Atomic data type has conflicting permitted and forbidden values", atomicDataType, ERROR);
            }

        }

        return save(atomicDataType.getPid(), result);
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

        if (typeProfile.getInheritsFrom() != null && typeProfile.getInheritsFrom().size() > 0) {
            for (TypeProfile parent : typeProfile.getInheritsFrom()) {
                if (parent.isAbstract() && !typeProfile.isAbstract()) {
                    result.addMessage("TypeProfile " + typeProfile.getPid() + " is not abstract, but inherits from the TypeProfile " + parent.getPid() + " that is abstract.", typeProfile, ERROR);
                }
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

        if (operationStep.getExecuteOperation() != null)
            result.addChild(operationStep.getExecuteOperation().execute(this, args));
        if (operationStep.getUseTechnology() != null)
            result.addChild(operationStep.getUseTechnology().execute(this, args));

        if (operationStep.getInputMappings() != null) {
            for (AttributeMapping attributeMapping : operationStep.getInputMappings()) {
                result.addChild(attributeMapping.execute(this, args));
            }
        }

        if (operationStep.getOutputMappings() != null) {
            for (AttributeMapping attributeMapping : operationStep.getOutputMappings()) {
                result.addChild(attributeMapping.execute(this, args));
            }
        }

        if (operationStep.getSubSteps() != null) {
            for (OperationStep child : operationStep.getSubSteps()) {
                result.addChild(child.execute(this, args));
            }
        }

        return save(operationStep.getId(), result);
    }

    @Override
    public ValidationResult visit(TechnologyInterface technologyInterface, Object... args) {
        ValidationResult result;
        if ((result = checkCache(technologyInterface.getPid())) != null) return result;
        else result = new ValidationResult();

        if (technologyInterface.getAttributes() != null) {
            for (Attribute attribute : technologyInterface.getAttributes()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        if (technologyInterface.getOutputs() != null) {
            for (Attribute attribute : technologyInterface.getOutputs()) {
                result.addChild(attribute.execute(this, args));
            }
        }

        return save(technologyInterface.getPid(), result);
    }
}
