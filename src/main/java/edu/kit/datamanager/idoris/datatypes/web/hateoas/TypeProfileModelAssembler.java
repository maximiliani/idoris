/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.datatypes.web.hateoas;

import edu.kit.datamanager.idoris.core.domain.web.hateoas.EntityModelAssembler;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler for converting TypeProfile entities to EntityModel objects with HATEOAS links.
 */
@Component
public class TypeProfileModelAssembler implements EntityModelAssembler<TypeProfile> {

    /**
     * Converts a TypeProfile entity to an EntityModel with HATEOAS links.
     *
     * @param typeProfile the TypeProfile entity to convert
     * @return an EntityModel containing the TypeProfile and links
     */
    @Override
    public EntityModel<TypeProfile> toModel(TypeProfile typeProfile) {
        EntityModel<TypeProfile> entityModel = toModelWithoutLinks(typeProfile);

        // Add self link
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(typeProfile.getPid())).withSelfRel());

        // Add link to validate
        entityModel.add(linkTo(methodOn(TypeProfileController.class).validate(typeProfile.getPid())).withRel("validate"));

        // Add link to inherited attributes
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(typeProfile.getPid())).withRel("inheritedAttributes"));

        // Add link to inheritance tree
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(typeProfile.getPid())).withRel("inheritanceTree"));

        // Add link to operations
        WebMvcLinkBuilder operationsLinkBuilder = linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(typeProfile.getPid()));
        entityModel.add(operationsLinkBuilder.withRel("operations"));

        // Add link to all type profiles
        entityModel.add(linkTo(methodOn(TypeProfileController.class).getAllTypeProfiles()).withRel("typeProfiles"));

        return entityModel;
    }
}