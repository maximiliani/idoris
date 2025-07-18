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

package edu.kit.datamanager.idoris.attributes.web.v1;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.attributes.services.AttributeService;
import edu.kit.datamanager.idoris.attributes.web.api.IAttributeApi;
import edu.kit.datamanager.idoris.attributes.web.hateoas.AttributeModelAssembler;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.DataTypeModelAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for Attribute entities.
 * This controller provides endpoints for managing Attribute entities.
 */
@RestController
@RequestMapping("/v1/attributes")
public class AttributeController implements IAttributeApi {

    private final AttributeService attributeService;
    private final AttributeModelAssembler attributeModelAssembler;
    private final DataTypeModelAssembler dataTypeModelAssembler;

    public AttributeController(AttributeService attributeService, AttributeModelAssembler attributeModelAssembler, DataTypeModelAssembler dataTypeModelAssembler) {
        this.attributeService = attributeService;
        this.attributeModelAssembler = attributeModelAssembler;
        this.dataTypeModelAssembler = dataTypeModelAssembler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAllAttributes() {
        List<EntityModel<Attribute>> attributes = attributeService.getAllAttributes().stream()
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
                .map(Attribute::getDataType)
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
    public ResponseEntity<EntityModel<Attribute>> updateAttribute(String id, Attribute attribute) {
        // Check if the entity exists
        if (attributeService.getAttribute(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        Attribute existing = attributeService.getAttribute(id).get();

        // Set the PID from the existing entity
        attribute.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        attribute.setInternalId(existing.getInternalId());

        Attribute updatedAttribute = attributeService.updateAttribute(attribute);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(updatedAttribute);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteAttribute(String pid) {
        if (attributeService.getAttribute(pid).isEmpty()) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<Attribute>> patchAttribute(String pid, Attribute attributePatch) {
        if (attributeService.getAttribute(pid).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Attribute patchedAttribute = attributeService.patchAttribute(pid, attributePatch);
        EntityModel<Attribute> entityModel = attributeModelAssembler.toModel(patchedAttribute);
        return ResponseEntity.ok(entityModel);
    }
}
