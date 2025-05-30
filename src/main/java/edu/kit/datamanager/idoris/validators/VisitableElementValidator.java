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

package edu.kit.datamanager.idoris.validators;

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.visitors.InheritanceValidator;
import edu.kit.datamanager.idoris.visitors.SyntaxValidator;
import edu.kit.datamanager.idoris.visitors.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Set;

@Log
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
                new ValidationPolicyValidator(),
                new SyntaxValidator(),
                new InheritanceValidator()
        );

        validators.forEach(validator -> {
            log.info("Executing " + validator.getClass().getName() + " on " + visitableElement.getClass().getName() + "...");
            ValidationResult validationResult = visitableElement.execute(validator);
            var validationMessages = validationResult.getValidationMessages()
                    .entrySet()
                    .stream()
                    .filter(e -> e.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
                    .filter(e -> !e.getValue().isEmpty())
                    .toArray();

            if (validationMessages.length == 0) return;

            switch (applicationProperties.getValidationPolicy()) {
                case STRICT -> {
                    log.warning("STRICT Validation failed: " + validationResult);
                    errors.rejectValue(null, validator.getClass().getSimpleName(), validationMessages, "Validation with " + validator.getClass().getSimpleName() + " failed.");
                }
                case LAX -> {
                    if (!validationResult.isValid()) {
                        log.warning("LAX Validation failed: " + validationResult);
                        errors.rejectValue(null, validator.getClass().getSimpleName(), validationMessages, "Validation with " + validator.getClass().getSimpleName() + " failed.");
                    }
                }
            }
        });
    }
}
