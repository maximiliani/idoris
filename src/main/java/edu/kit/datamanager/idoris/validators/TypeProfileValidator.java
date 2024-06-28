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
import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import lombok.AllArgsConstructor;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

//@Component("beforeSaveTypeProfileValidator")
@AllArgsConstructor
public class TypeProfileValidator implements Validator {
    //    @Autowired
    ITypeProfileDao typeProfileDao;

    @Override
    public boolean supports(Class<?> clazz) {
        return TypeProfile.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TypeProfile typeProfile = (TypeProfile) target;
        validateBasicInformation(typeProfile, errors);
        validateRecursively(typeProfile, errors);
    }

    private void validateBasicInformation(TypeProfile typeProfile, Errors errors) {
        if (typeProfile.getName() == null || typeProfile.getName().isEmpty()) {
            errors.rejectValue("name", "name.empty", "Name must not be empty.");
        }
        if (typeProfile.getDescription() == null || typeProfile.getDescription().isEmpty()) {
            errors.rejectValue("description", "description.empty", "Description must not be empty.");
        }
    }

    private void validateRecursively(TypeProfile typeProfile, Errors errors) {
    }

    private record ValidationDTO(
            TypeProfile self,
            List<ProfileAttribute> inheritedAttributes,
            List<ValidationDTO> validatedParents
    ) {
    }
}
