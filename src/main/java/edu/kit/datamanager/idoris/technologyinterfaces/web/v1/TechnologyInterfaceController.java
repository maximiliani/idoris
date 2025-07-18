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

package edu.kit.datamanager.idoris.technologyinterfaces.web.v1;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.attributes.web.hateoas.AttributeModelAssembler;
import edu.kit.datamanager.idoris.technologyinterfaces.entities.TechnologyInterface;
import edu.kit.datamanager.idoris.technologyinterfaces.services.TechnologyInterfaceService;
import edu.kit.datamanager.idoris.technologyinterfaces.web.api.ITechnologyInterfaceApi;
import edu.kit.datamanager.idoris.technologyinterfaces.web.hateoas.TechnologyInterfaceModelAssembler;
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
 * REST controller for TechnologyInterface entities.
 * This controller provides endpoints for managing TechnologyInterface entities.
 */
@RestController
@RequestMapping("/v1/technologyInterfaces")
public class TechnologyInterfaceController implements ITechnologyInterfaceApi {

    @Autowired
    private TechnologyInterfaceService technologyInterfaceService;

    @Autowired
    private TechnologyInterfaceModelAssembler technologyInterfaceModelAssembler;

    @Autowired
    private AttributeModelAssembler attributeModelAssembler;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CollectionModel<EntityModel<TechnologyInterface>>> getAllTechnologyInterfaces() {
        List<EntityModel<TechnologyInterface>> technologyInterfaces = StreamSupport.stream(technologyInterfaceService.getAllTechnologyInterfaces().spliterator(), false)
                .map(technologyInterfaceModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TechnologyInterface>> collectionModel = CollectionModel.of(
                technologyInterfaces,
                linkTo(methodOn(TechnologyInterfaceController.class).getAllTechnologyInterfaces()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<TechnologyInterface>> getTechnologyInterface(String id) {
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterfaceModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getAttributes(String id) {
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterface -> {
                    List<EntityModel<Attribute>> attributes = StreamSupport.stream(technologyInterface.getAttributes().spliterator(), false)
                            .map(attributeModelAssembler::toModel)
                            .collect(Collectors.toList());

                    CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                            attributes,
                            linkTo(methodOn(TechnologyInterfaceController.class).getAttributes(id)).withSelfRel(),
                            linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(id)).withRel("technologyInterface")
                    );

                    return ResponseEntity.ok(collectionModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getOutputs(String id) {
        return technologyInterfaceService.getTechnologyInterface(id)
                .map(technologyInterface -> {
                    List<EntityModel<Attribute>> outputs = StreamSupport.stream(technologyInterface.getOutputs().spliterator(), false)
                            .map(attributeModelAssembler::toModel)
                            .collect(Collectors.toList());

                    CollectionModel<EntityModel<Attribute>> collectionModel = CollectionModel.of(
                            outputs,
                            linkTo(methodOn(TechnologyInterfaceController.class).getOutputs(id)).withSelfRel(),
                            linkTo(methodOn(TechnologyInterfaceController.class).getTechnologyInterface(id)).withRel("technologyInterface")
                    );

                    return ResponseEntity.ok(collectionModel);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<TechnologyInterface>> createTechnologyInterface(TechnologyInterface technologyInterface) {
        TechnologyInterface createdTechnologyInterface = technologyInterfaceService.createTechnologyInterface(technologyInterface);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(createdTechnologyInterface);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<TechnologyInterface>> updateTechnologyInterface(String id, TechnologyInterface technologyInterface) {
        // Check if the entity exists
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        TechnologyInterface existing = technologyInterfaceService.getTechnologyInterface(id).get();

        // Set the PID from the existing entity
        technologyInterface.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        technologyInterface.setInternalId(existing.getInternalId());

        TechnologyInterface updatedTechnologyInterface = technologyInterfaceService.updateTechnologyInterface(technologyInterface);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(updatedTechnologyInterface);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteTechnologyInterface(String id) {
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        technologyInterfaceService.deleteTechnologyInterface(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EntityModel<TechnologyInterface>> patchTechnologyInterface(String id, TechnologyInterface technologyInterfacePatch) {
        if (!technologyInterfaceService.getTechnologyInterface(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        TechnologyInterface patchedTechnologyInterface = technologyInterfaceService.patchTechnologyInterface(id, technologyInterfacePatch);
        EntityModel<TechnologyInterface> entityModel = technologyInterfaceModelAssembler.toModel(patchedTechnologyInterface);
        return ResponseEntity.ok(entityModel);
    }
}
