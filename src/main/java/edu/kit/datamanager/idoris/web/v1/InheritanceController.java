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

package edu.kit.datamanager.idoris.web.v1;

import edu.kit.datamanager.idoris.dao.IBasicDataTypeDao;
import edu.kit.datamanager.idoris.dao.IDataTypeDao;
import edu.kit.datamanager.idoris.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.domain.entities.Operation;
import edu.kit.datamanager.idoris.domain.entities.TypeProfile;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RepositoryRestController
public class InheritanceController {
    @Autowired
    ITypeProfileDao typeProfileDao;

    @Autowired
    IBasicDataTypeDao basicDataTypeDao;

    @Autowired
    IDataTypeDao dataTypeDao;

    @GetMapping("typeProfiles/{pid}/inheritedAttributes")
    public ResponseEntity getInheritedAttributes(@PathVariable("pid") String pid) throws NoSuchMethodException {
        Iterable<TypeProfile> inheritanceChain = typeProfileDao.findAllInInheritanceChain(pid);
        List<ProfileAttributeDTO> attributes = new ArrayList<>();
        inheritanceChain.forEach(typeProfile -> {
            typeProfileDao.findById(typeProfile.getPid())
                    .orElseThrow()
                    .getAttributes()
                    .forEach(profileAttribute -> {
                        attributes.add(new ProfileAttributeDTO(
                                profileAttribute,
                                linkTo(IDataTypeDao.class)
                                        .slash("api")
                                        .slash("dataTypes")
                                        .slash(profileAttribute.getDataType().getPid())
                                        .withRel("dataType")
                        ));
                    });
        });
        CollectionModel<ProfileAttributeDTO> resources = CollectionModel.of(attributes);
        resources.add(
                linkTo(InheritanceController.class)
                        .slash("api")
                        .slash("typeProfiles")
                        .slash(pid)
                        .slash("inheritedAttributes")
                        .withSelfRel());
        resources.add(
                linkTo(ITypeProfileDao.class)
                        .slash("api")
                        .slash("typeProfiles")
                        .slash(pid)
                        .withRel("typeProfile"));
        return ResponseEntity.ok(resources);
    }

    @GetMapping("dataTypes/{pid}/operations")
    public ResponseEntity getOperations(@PathVariable("pid") String pid) {
        Iterable<Operation> operations = dataTypeDao.getOperations(pid);
        CollectionModel<Operation> resources = CollectionModel.of(operations);
        return ResponseEntity.ok(resources);
    }

    public record ProfileAttributeDTO(ProfileAttribute attribute, Link dataType) {
    }
}
