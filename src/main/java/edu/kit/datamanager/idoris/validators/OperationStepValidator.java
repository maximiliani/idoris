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

import edu.kit.datamanager.idoris.domain.entities.OperationStep;
import lombok.extern.java.Log;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Log
public class OperationStepValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return OperationStep.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OperationStep operationStep = (OperationStep) target;
        log.info("Validating operation step: " + operationStep);

        if (operationStep.getName() == null || operationStep.getName().isEmpty()) {
            errors.rejectValue(null, "name.unspecified", "For better human readability and understanding, you MUST provide a name for the operation step.");
        }

        if (operationStep.getExecutionOrderIndex() == null || operationStep.getExecutionOrderIndex() < 0) {
            errors.rejectValue(null, "executionOrderIndex.unspecified", "You MUST specify the execution order index of the operation step. It must be a non-negative integer.");
        }

        if (operationStep.getMode() == null) {
            errors.rejectValue(null, "mode.empty", "You MUST specify the mode of the operation step.");
        }

        if (operationStep.getOperation() == null && operationStep.getOperationTypeProfile() == null) {
            errors.rejectValue(null, "operation.unspecified", "You MUST specify an operation or an operation type profile which is executed in this operation step.");
            errors.rejectValue(null, "operationTypeProfile.unspecified", "You MUST specify an operation or an operation type profile which is executed in this operation step.");
        }

        if (operationStep.getOperation() != null && operationStep.getOperationTypeProfile() != null) {
            errors.rejectValue(null, "operation.ambiguous", "The total number of operations and operation type profiles MUST be exactly one.");
            errors.rejectValue(null, "operationTypeProfile.ambiguous", "The total number of operations and operation type profiles MUST be exactly one.");
        }
    }
}
