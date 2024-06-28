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

import edu.kit.datamanager.idoris.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.domain.entities.AttributeMapping;
import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

//@Component("beforeSaveAttributeMappingValidator")
@AllArgsConstructor
public class AttributeMappingValidator implements Validator {
    ITypeProfileDao typeProfileDao;

    @Override
    public boolean supports(Class<?> clazz) {
        return TypeProfile.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AttributeMapping attributeMapping = (AttributeMapping) target;

        if (attributeMapping.getOutput() == null) {
            errors.rejectValue("output", "output.empty", "Output must not be empty.");
        }

        if (attributeMapping.getInput() == null && attributeMapping.getValue() == null) {
            errors.rejectValue("input", "input.empty", "Input and value must not be unspecified at the same time.");
            errors.rejectValue("value", "value.empty", "Value and input must not be unspecified at the same time.");
        }

//        if (attributeMapping.getInput().getDataType().)
    }
}
