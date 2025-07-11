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

package edu.kit.datamanager.idoris.web.v1;

import edu.kit.datamanager.idoris.dao.IDataTypeDao;
import edu.kit.datamanager.idoris.dao.IOperationDao;
import edu.kit.datamanager.idoris.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.domain.entities.Attribute;
import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import edu.kit.datamanager.idoris.rules.validation.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RepositoryRestController
public class TypeProfileController {
    @Autowired
    ITypeProfileDao typeProfileDao;

    @Autowired
    IDataTypeDao dataTypeDao;

    @GetMapping("typeProfiles/{pid}/validate")
    public ResponseEntity<?> validate(@PathVariable("pid") String pid) {
        TypeProfile typeProfile = typeProfileDao.findById(pid).orElseThrow();
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult result = typeProfile.execute(validator);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(218).body(result);
        }
    }

    @GetMapping("typeProfiles/{pid}/inheritedAttributes")
    public ResponseEntity<?> getInheritedAttributes(@PathVariable("pid") String pid) {
        Iterable<TypeProfile> inheritanceChain = typeProfileDao.findAllTypeProfilesInInheritanceChain(pid);
        List<EntityModel<Attribute>> attributes = new ArrayList<>();
        inheritanceChain.forEach(typeProfile -> {
            typeProfileDao.findById(typeProfile.getPid()).orElseThrow().getAttributes().forEach(profileAttribute -> {
                EntityModel<Attribute> attribute = EntityModel.of(profileAttribute);
                attribute.add(linkTo(IDataTypeDao.class).slash("api").slash("dataTypes").slash(profileAttribute.getDataType().getPid()).withRel("dataType"));
                attributes.add(attribute);
            });
        });
        CollectionModel<EntityModel<Attribute>> resources = CollectionModel.of(attributes);
        resources.add(linkTo(TypeProfileController.class).slash("api").slash("typeProfiles").slash(pid).slash("inheritedAttributes").withSelfRel());
        resources.add(linkTo(ITypeProfileDao.class).slash("api").slash("typeProfiles").slash(pid).withRel("typeProfile"));
        return ResponseEntity.ok(resources);
    }

    @GetMapping("typeProfiles/{pid}/inheritanceTree")
    public HttpEntity<EntityModel<TypeProfileInheritance>> getInheritanceTree(@NotNull @PathVariable("pid") String pid) {
        EntityModel<TypeProfileInheritance> resources = buildInheritanceTree(typeProfileDao.findById(pid).orElseThrow());
        resources.add(linkTo(TypeProfileController.class).slash("api").slash("typeProfiles").slash(pid).slash("inheritanceTree").withSelfRel());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    private EntityModel<TypeProfileInheritance> buildInheritanceTree(TypeProfile typeProfile) {
        List<EntityModel<Attribute>> attributes = new ArrayList<>();
        typeProfile.getAttributes().forEach(profileAttribute -> {
            EntityModel<Attribute> attribute = EntityModel.of(profileAttribute);
            attribute.add(linkTo(IDataTypeDao.class).slash("api").slash("dataTypes").slash(profileAttribute.getDataType().getPid()).withRel("dataType"));
            attributes.add(attribute);
        });

        List<EntityModel<TypeProfileInheritance>> inheritsFrom = new ArrayList<>();
        typeProfile.getInheritsFrom().forEach(inheritedTypeProfile -> {
            inheritsFrom.add(buildInheritanceTree(inheritedTypeProfile));
        });

        EntityModel<TypeProfileInheritance> node = EntityModel.of(
                new TypeProfileInheritance(typeProfile.getPid(),
                        typeProfile.getName(),
                        typeProfile.getDescription(),
                        CollectionModel.of(attributes),
                        CollectionModel.of(inheritsFrom)));

        node.add(linkTo(TypeProfileController.class)
                .slash("api")
                .slash("typeProfiles")
                .slash(typeProfile.getPid())
                .slash("inheritanceTree")
                .withRel("inheritanceTree"));
        node.add(linkTo(ITypeProfileDao.class)
                .slash("api")
                .slash("typeProfiles")
                .slash(typeProfile.getPid())
                .withRel("typeProfile"));
        node.add(linkTo(TypeProfileController.class)
                .slash("api")
                .slash("typeProfiles")
                .slash(typeProfile.getPid())
                .slash("attributes")
                .withRel("attributes"));
        node.add(linkTo(TypeProfileController.class)
                .slash("api")
                .slash("typeProfiles")
                .slash(typeProfile.getPid())
                .slash("inheritedAttributes")
                .withRel("inheritedAttributes"));
        node.add(Link.of(
                linkTo(IOperationDao.class)
                        .slash("api")
                        .slash("operations")
                        .slash("search")
                        .slash("getOperationsForDataType")
                        .toUri() + "?pid=" + typeProfile.getPid(),
                "operations")
        );
        return node;
    }

    public record TypeProfileInheritance(
            String pid,
            String name,
            String description,
            CollectionModel<EntityModel<Attribute>> attributes,
            CollectionModel<EntityModel<TypeProfileInheritance>> inheritsFrom) {
    }
}
