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

package edu.kit.datamanager.idoris.web.v1;

import edu.kit.datamanager.idoris.domain.entities.Attribute;
import edu.kit.datamanager.idoris.domain.entities.DataType;
import edu.kit.datamanager.idoris.services.AttributeService;
import edu.kit.datamanager.idoris.web.api.IAttributeApi;
import edu.kit.datamanager.idoris.web.hateoas.AttributeModelAssembler;
import edu.kit.datamanager.idoris.web.hateoas.DataTypeModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for Attribute entities.
 * This controller provides endpoints for managing Attribute entities.
 */
@RestController
@RequestMapping("/v1/attributes")
public class AttributeController implements IAttributeApi {

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private AttributeModelAssembler attributeModelAssembler;

    @Autowired
    private DataTypeModelAssembler dataTypeModelAssembler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAllAttributes() {
        List<EntityModel<Attribute>> attributes = StreamSupport.stream(attributeService.getAllAttributes().spliterator(), false)
                .map(attributeModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                attributes,
                linkTo(methodOn(AttributeController.class).getAllAttributes()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<Attribute>> getAttribute(String pid) {
        return attributeService.getAttribute(pid)
                .map(attributeModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<DataType>> getDataType(String pid) {
        return attributeService.getAttribute(pid)
                .map(attribute -> attribute.getDataType())
                .map(dataTypeModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<Attribute>> createAttribute(Attribute attribute) {
        Attribute createdAttribute = attributeService.createAttribute(attribute);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(createdAttribute);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<Attribute>> updateAttribute(String pid, Attribute attribute) {
        if (!attributeService.getAttribute(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        attribute.setPid(pid);
        Attribute updatedAttribute = attributeService.updateAttribute(attribute);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(updatedAttribute);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAttribute(String pid) {
        if (!attributeService.getAttribute(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        attributeService.deleteAttribute(pid);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteOrphanedAttributes() {
        attributeService.deleteOrphanedAttributes();
        return ResponseEntity.noContent().build();
    }
}
