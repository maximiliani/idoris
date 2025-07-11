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

import edu.kit.datamanager.idoris.domain.entities.Operation;
import edu.kit.datamanager.idoris.domain.services.OperationService;
import edu.kit.datamanager.idoris.rules.validation.ValidationPolicyValidator;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import edu.kit.datamanager.idoris.web.hateoas.OperationModelAssembler;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
 * REST controller for Operation entities.
 * This controller provides endpoints for managing Operation entities.
 */
@RestController
@RequestMapping("/api/operations")
@Tag(name = "Operation", description = "API for managing Operations")
public class OperationController {

    @Autowired
    private OperationService operationService;

    @Autowired
    private OperationModelAssembler operationModelAssembler;

    /**
     * Gets all Operation entities.
     *
     * @return a collection of all Operation entities
     */
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all Operations",
            description = "Returns a collection of all Operation entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class)))
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getAllOperations() {
        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getAllOperations().spliterator(), false)
                .map(operationModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(OperationController.class).getAllOperations()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Gets an Operation entity by its PID.
     *
     * @param pid the PID of the Operation to retrieve
     * @return the Operation entity
     */
    @GetMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get an Operation by PID",
            description = "Returns an Operation entity by its PID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    public ResponseEntity<EntityModel<Operation>> getOperation(
            @Parameter(description = "PID of the Operation", required = true)
            @PathVariable String pid) {
        return operationService.getOperation(pid)
                .map(operationModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new Operation entity.
     *
     * @param operation the Operation entity to create
     * @return the created Operation entity
     */
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new Operation",
            description = "Creates a new Operation entity",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Operation created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    public ResponseEntity<EntityModel<Operation>> createOperation(
            @Parameter(description = "Operation to create", required = true)
            @Valid @RequestBody Operation operation) {
        Operation createdOperation = operationService.createOperation(operation);
        EntityModel<Operation> entityModel = operationModelAssembler.toModel(createdOperation);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * Updates an existing Operation entity.
     *
     * @param pid       the PID of the Operation to update
     * @param operation the updated Operation entity
     * @return the updated Operation entity
     */
    @PutMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update an Operation",
            description = "Updates an existing Operation entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    public ResponseEntity<EntityModel<Operation>> updateOperation(
            @Parameter(description = "PID of the Operation", required = true)
            @PathVariable String pid,
            @Parameter(description = "Updated Operation", required = true)
            @Valid @RequestBody Operation operation) {
        if (!operationService.getOperation(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        operation.setPid(pid);
        Operation updatedOperation = operationService.updateOperation(operation);
        EntityModel<Operation> entityModel = operationModelAssembler.toModel(updatedOperation);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * Deletes an Operation entity.
     *
     * @param pid the PID of the Operation to delete
     * @return no content
     */
    @DeleteMapping("/{pid}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete an Operation",
            description = "Deletes an Operation entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Operation deleted"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    public ResponseEntity<Void> deleteOperation(
            @Parameter(description = "PID of the Operation", required = true)
            @PathVariable String pid) {
        if (!operationService.getOperation(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        operationService.deleteOperation(pid);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates an Operation entity.
     *
     * @param pid the PID of the Operation to validate
     * @return the validation result
     */
    @GetMapping("/{pid}/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Validate an Operation",
            description = "Validates an Operation entity and returns the validation result",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation is valid"),
                    @ApiResponse(responseCode = "218", description = "Operation is invalid"),
                    @ApiResponse(responseCode = "404", description = "Operation not found")
            }
    )
    public ResponseEntity<?> validate(
            @Parameter(description = "PID of the Operation", required = true)
            @PathVariable String pid) {
        if (!operationService.getOperation(pid).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Operation operation = operationService.getOperation(pid).get();
        ValidationPolicyValidator validator = new ValidationPolicyValidator();
        ValidationResult result = operation.execute(validator);

        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(218).body(result);
        }
    }

    /**
     * Gets operations for a data type.
     *
     * @param pid the PID of the data type
     * @return a collection of operations for the data type
     */
    @GetMapping("/search/getOperationsForDataType")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a data type",
            description = "Returns a collection of operations that can be executed on a data type",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class)))
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForDataType(
            @Parameter(description = "PID of the data type", required = true)
            @RequestParam String pid) {
        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getOperationsForDataType(pid).spliterator(), false)
                .map(operationModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(OperationController.class).getOperationsForDataType(pid)).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }
}
