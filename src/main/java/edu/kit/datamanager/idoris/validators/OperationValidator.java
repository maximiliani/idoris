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

package edu.kit.datamanager.idoris.validators;

import edu.kit.datamanager.idoris.domain.entities.Operation;
import edu.kit.datamanager.idoris.domain.entities.OperationStep;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

//@Component("beforeSaveOperationValidator")
public class OperationValidator implements Validator {
    private final Validator operationStepValidator;

    public OperationValidator(Validator operationStepValidator) {
        if (operationStepValidator == null) {
            throw new IllegalArgumentException("The supplied [Validator] is " +
                    "required and must not be null.");
        }
        if (!operationStepValidator.supports(OperationStep.class)) {
            throw new IllegalArgumentException("The supplied [Validator] must " +
                    "support the validation of [Address] instances.");
        }
        this.operationStepValidator = operationStepValidator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Operation.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Operation operation = (Operation) target;

        if (operation.getName() == null || operation.getName().isEmpty()) {
            errors.rejectValue("name", "name.empty", "For better human readability and understanding, you MUST provide a name for the operation.");
        }

        if (operation.getExecutableOn() == null) {
            errors.rejectValue("executableOn", "executableOn.empty", "You MUST specify the data type on which the operation can be executed.");
        }

        if (operation.getExecution() == null || operation.getExecution().isEmpty()) {
            errors.rejectValue("execution", "execution.empty", "You MUST specify the steps of the operation.");
        }

        try {
            errors.pushNestedPath("execution");
            for (OperationStep operationStep : operation.getExecution()) {
                ValidationUtils.invokeValidator(this.operationStepValidator, operationStep, errors);
            }
        } finally {
            errors.popNestedPath();
        }
    }
}
