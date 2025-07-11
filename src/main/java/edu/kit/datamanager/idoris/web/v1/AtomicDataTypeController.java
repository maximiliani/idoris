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

import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.domain.entities.AtomicDataType;
import edu.kit.datamanager.idoris.domain.entities.Operation;
import edu.kit.datamanager.idoris.domain.services.AtomicDataTypeService;
import edu.kit.datamanager.idoris.domain.services.OperationService;
import edu.kit.datamanager.idoris.rules.logic.RuleService;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import edu.kit.datamanager.idoris.web.ValidationException;
import edu.kit.datamanager.idoris.web.hateoas.AtomicDataTypeModelAssembler;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for AtomicDataType entities.
 * This controller provides endpoints for managing AtomicDataType entities.
 */
@RestController
@RequestMapping("/v1/api/atomicDataTypes")
@Tag(name = "AtomicDataType", description = "API for managing AtomicDataTypes")
@Slf4j
public class AtomicDataTypeController {

    private final AtomicDataTypeService atomicDataTypeService;
    private final OperationService operationService;
    private final AtomicDataTypeModelAssembler atomicDataTypeModelAssembler;
    private final RuleService ruleService;
    private final ApplicationProperties applicationProperties;

    public AtomicDataTypeController(AtomicDataTypeService atomicDataTypeService, OperationService operationService, AtomicDataTypeModelAssembler atomicDataTypeModelAssembler, RuleService ruleService, ApplicationProperties applicationProperties) {
        this.atomicDataTypeService = atomicDataTypeService;
        this.operationService = operationService;
        this.atomicDataTypeModelAssembler = atomicDataTypeModelAssembler;
        this.ruleService = ruleService;
        this.applicationProperties = applicationProperties;
    }

    /**
     * Gets all AtomicDataType entities.
     *
     * @return a collection of all AtomicDataType entities
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all AtomicDataTypes",
            description = "Returns a collection of all AtomicDataType entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataTypes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class)))
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<AtomicDataType>>> getAllAtomicDataTypes() {
        List<EntityModel<AtomicDataType>> atomicDataTypes = StreamSupport.stream(atomicDataTypeService.getAllAtomicDataTypes().spliterator(), false)
                .map(atomicDataTypeModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<AtomicDataType>> collectionModel = CollectionModel.of(
                atomicDataTypes,
                linkTo(methodOn(AtomicDataTypeController.class).getAllAtomicDataTypes()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Gets an AtomicDataType entity by its PID.
     *
     * @param pid the PID of the AtomicDataType to retrieve
     * @return the AtomicDataType entity
     */
    @GetMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get an AtomicDataType by PID",
            description = "Returns an AtomicDataType entity by its PID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    public ResponseEntity<EntityModel<AtomicDataType>> getAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid) {
        return atomicDataTypeService.getAtomicDataType(pid)
                .map(atomicDataTypeModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new AtomicDataType entity.
     *
     * @param atomicDataType the AtomicDataType entity to create
     * @return the created AtomicDataType entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new AtomicDataType",
            description = "Creates a new AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "201", description = "AtomicDataType created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<EntityModel<AtomicDataType>> createAtomicDataType(
            @Parameter(description = "AtomicDataType to create", required = true)
            @RequestBody AtomicDataType atomicDataType) {

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                atomicDataType,
                ValidationResult::new
        );
        log.debug("Validation result for AtomicDataType {}: {}", atomicDataType, validationResult);

        // Check if validation failed based on your validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        // Only save if validation passes
        AtomicDataType saved = atomicDataTypeService.createAtomicDataType(atomicDataType);
        return ResponseEntity.status(HttpStatus.CREATED).body(EntityModel.of(saved));

    }

    /**
     * Updates an existing AtomicDataType entity.
     *
     * @param pid            the PID of the AtomicDataType to update
     * @param atomicDataType the updated AtomicDataType entity
     * @return the updated AtomicDataType entity
     */
    @PutMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an AtomicDataType",
            description = "Updates an existing AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    public ResponseEntity<EntityModel<AtomicDataType>> updateAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid,
            @Parameter(description = "Updated AtomicDataType", required = true)
            @Valid @RequestBody AtomicDataType atomicDataType) {
        if (!atomicDataTypeService.getAtomicDataType(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        atomicDataType.setPid(pid);
        AtomicDataType updatedAtomicDataType = atomicDataTypeService.updateAtomicDataType(atomicDataType);
        EntityModel<AtomicDataType> entityModel = atomicDataTypeModelAssembler.toModel(updatedAtomicDataType);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Deletes an AtomicDataType entity.
     *
     * @param pid the PID of the AtomicDataType to delete
     * @return no content
     */
    @DeleteMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an AtomicDataType",
            description = "Deletes an AtomicDataType entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "AtomicDataType deleted"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    public ResponseEntity<Void> deleteAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid) {
        if (!atomicDataTypeService.getAtomicDataType(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        atomicDataTypeService.deleteAtomicDataType(pid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets operations for an AtomicDataType.
     *
     * @param pid the PID of the AtomicDataType
     * @return a collection of operations for the AtomicDataType
     */
    @GetMapping("/{pid}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for an AtomicDataType",
            description = "Returns a collection of operations that can be executed on an AtomicDataType",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid) {
        if (!atomicDataTypeService.getAtomicDataType(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getOperationsForDataType(pid).spliterator(), false)
                .map(operation -> EntityModel.of(operation,
                        linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(pid)).withSelfRel(),
                        linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(pid)).withRel("atomicDataType")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(pid)).withSelfRel(),
                linkTo(methodOn(AtomicDataTypeController.class).getAtomicDataType(pid)).withRel("atomicDataType")
        );

        return ResponseEntity.ok(collectionModel);
    }

    private boolean hasValidationErrors(ValidationResult validationResult) {
        return validationResult.getOutputMessages()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel())
                        && !entry.getValue().isEmpty());
    }

}