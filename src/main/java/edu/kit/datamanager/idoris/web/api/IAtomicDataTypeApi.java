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

package edu.kit.datamanager.idoris.web.api;

import edu.kit.datamanager.idoris.domain.entities.AtomicDataType;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API interface for AtomicDataType endpoints.
 * This interface defines the REST API for managing AtomicDataType entities.
 */
@Tag(name = "AtomicDataType", description = "API for managing AtomicDataTypes")
public interface IAtomicDataTypeApi {

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
    ResponseEntity<CollectionModel<EntityModel<AtomicDataType>>> getAllAtomicDataTypes();

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
    ResponseEntity<EntityModel<AtomicDataType>> getAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid);

    /**
     * Creates a new AtomicDataType entity.
     * The entity is validated before saving.
     *
     * @param atomicDataType the AtomicDataType entity to create
     * @return the created AtomicDataType entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new AtomicDataType",
            description = "Creates a new AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "AtomicDataType created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    ResponseEntity<EntityModel<AtomicDataType>> createAtomicDataType(
            @Parameter(description = "AtomicDataType to create", required = true)
            @Valid @RequestBody AtomicDataType atomicDataType);

    /**
     * Updates an existing AtomicDataType entity.
     * The entity is validated before saving.
     *
     * @param pid            the PID of the AtomicDataType to update
     * @param atomicDataType the updated AtomicDataType entity
     * @return the updated AtomicDataType entity
     */
    @PutMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an AtomicDataType",
            description = "Updates an existing AtomicDataType entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AtomicDataType updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = AtomicDataType.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    ResponseEntity<EntityModel<AtomicDataType>> updateAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid,
            @Parameter(description = "Updated AtomicDataType", required = true)
            @Valid @RequestBody AtomicDataType atomicDataType);

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
    ResponseEntity<Void> deleteAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid);

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
                                    schema = @Schema(implementation = edu.kit.datamanager.idoris.domain.entities.Operation.class))),
                    @ApiResponse(responseCode = "404", description = "AtomicDataType not found")
            }
    )
    ResponseEntity<CollectionModel<EntityModel<edu.kit.datamanager.idoris.domain.entities.Operation>>> getOperationsForAtomicDataType(
            @Parameter(description = "PID of the AtomicDataType", required = true)
            @PathVariable String pid);
}
