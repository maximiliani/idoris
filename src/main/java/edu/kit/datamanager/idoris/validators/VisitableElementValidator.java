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

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.visitors.*;
import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

@AllArgsConstructor
public class VisitableElementValidator implements Validator {
    private ApplicationProperties applicationProperties;

    @Override
    public boolean supports(Class<?> clazz) {
        return VisitableElement.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        VisitableElement visitableElement = (VisitableElement) target;

        Set<Visitor<ValidationResult>> validators = Set.of(
                new SubSchemaRelationValidator(),
                new SyntaxValidator(),
                new InheritanceValidator()
        );

        validators.forEach(validator -> {
            ValidationResult validationResult = visitableElement.execute(validator);
            switch (applicationProperties.getValidationPolicy()) {
                case STRICT -> {
                    if (!validationResult.isEmpty()) {
                        errors.rejectValue(null, validator.getClass().getName() + " failed", new ValidationResult[]{validationResult}, "Validation failed.");
                    }
                }
                case LAX -> {
                    if (!validationResult.isValid()) {
                        errors.rejectValue(null, validator.getClass().getName() + " failed", new ValidationResult[]{validationResult}, "Validation failed.");
                    }
                }
            }
        });
    }
}
