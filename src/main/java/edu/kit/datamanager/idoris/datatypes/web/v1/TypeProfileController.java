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

package edu.kit.datamanager.idoris.datatypes.web.v1;

import edu.kit.datamanager.idoris.attributes.entities.Attribute;
import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.core.domain.exceptions.ValidationException;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.services.TypeProfileService;
import edu.kit.datamanager.idoris.datatypes.web.api.ITypeProfileApi;
import edu.kit.datamanager.idoris.datatypes.web.hateoas.TypeProfileModelAssembler;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import edu.kit.datamanager.idoris.operations.services.OperationService;
import edu.kit.datamanager.idoris.rules.logic.RuleService;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST controller for TypeProfile entities.
 * This controller provides endpoints for managing TypeProfile entities.
 */
@RestController
@RequestMapping("/v1/typeProfiles")
@Tag(name = "TypeProfile", description = "API for managing TypeProfiles")
public class TypeProfileController implements ITypeProfileApi {
    private final TypeProfileService typeProfileService;
    private final OperationService operationService;
    private final TypeProfileModelAssembler typeProfileModelAssembler;
    private final RuleService ruleService;
    private final ApplicationProperties applicationProperties;

    public TypeProfileController(TypeProfileService typeProfileService,
                                 OperationService operationService,
                                 TypeProfileModelAssembler typeProfileModelAssembler,
                                 RuleService ruleService,
                                 ApplicationProperties applicationProperties) {
        this.typeProfileService = typeProfileService;
        this.operationService = operationService;
        this.typeProfileModelAssembler = typeProfileModelAssembler;
        this.ruleService = ruleService;
        this.applicationProperties = applicationProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get all TypeProfiles",
            description = "Returns a collection of all TypeProfile entities",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfiles found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class)))
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<TypeProfile>>> getAllTypeProfiles() {
        List<EntityModel<TypeProfile>> typeProfiles = StreamSupport.stream(typeProfileService.getAllTypeProfiles().spliterator(), false)
                .map(typeProfileModelAssembler::toModel)
                .collect(Collectors.toList());

        CollectionModel<EntityModel<TypeProfile>> collectionModel = CollectionModel.of(
                typeProfiles,
                linkTo(methodOn(TypeProfileController.class).getAllTypeProfiles()).withSelfRel()
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get a TypeProfile by PID or internal ID",
            description = "Returns a TypeProfile entity by its PID or internal ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<EntityModel<TypeProfile>> getTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id) {
        return typeProfileService.getTypeProfile(id)
                .map(typeProfileModelAssembler::toModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}/operations")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get operations for a TypeProfile",
            description = "Returns a collection of operations that can be executed on a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operations found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Operation.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<Operation>>> getOperationsForTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id) {
        if (!typeProfileService.getTypeProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        List<EntityModel<Operation>> operations = StreamSupport.stream(operationService.getOperationsForDataType(id).spliterator(), false)
                .map(operation -> EntityModel.of(operation,
                        linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(id)).withSelfRel(),
                        linkTo(methodOn(TypeProfileController.class).getTypeProfile(id)).withRel("typeProfile")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Operation>> collectionModel = CollectionModel.of(
                operations,
                linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(id)).withSelfRel(),
                linkTo(methodOn(TypeProfileController.class).getTypeProfile(id)).withRel("typeProfile")
        );

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}/validate")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Validate a TypeProfile",
            description = "Validates a TypeProfile entity and returns the validation result",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile is valid"),
                    @ApiResponse(responseCode = "218", description = "TypeProfile is invalid"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<?> validate(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id) {
        ValidationResult result = typeProfileService.validateTypeProfile(id);
        if (result.isValid()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(218).body(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}/inheritedAttributes")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inherited attributes of a TypeProfile",
            description = "Returns a collection of attributes inherited by a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inherited attributes found",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = Attribute.class))),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<CollectionModel<EntityModel<Attribute>>> getInheritedAttributes(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id) {
        Iterable<TypeProfile> inheritanceChain = typeProfileService.getInheritanceChain(id);
        List<EntityModel<Attribute>> attributes = new ArrayList<>();
        inheritanceChain.forEach(typeProfile -> {
            typeProfileService.getTypeProfile(typeProfile.getId()).orElseThrow().getAttributes().forEach(profileAttribute -> {
                EntityModel<Attribute> attribute = EntityModel.of(profileAttribute);
                attribute.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(profileAttribute.getDataType().getId())).withRel("dataType"));
                attributes.add(attribute);
            });
        });

        CollectionModel<EntityModel<Attribute>> resources = CollectionModel.of(attributes);
        resources.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(id)).withSelfRel());
        resources.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(id)).withRel("typeProfile"));

        return ResponseEntity.ok(resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Create a new TypeProfile",
            description = "Creates a new TypeProfile entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "201", description = "TypeProfile created",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed")
            }
    )
    public ResponseEntity<EntityModel<TypeProfile>> createTypeProfile(
            @Parameter(description = "TypeProfile to create", required = true)
            @Valid @RequestBody TypeProfile typeProfile) {

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                typeProfile,
                ValidationResult::new
        );

        // Check if validation failed based on your validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        // Only save if validation passes
        TypeProfile createdTypeProfile = typeProfileService.createTypeProfile(typeProfile);
        EntityModel<TypeProfile> entityModel = typeProfileModelAssembler.toModel(createdTypeProfile);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Update a TypeProfile",
            description = "Updates an existing TypeProfile entity after validating it",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile updated",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation failed"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<EntityModel<TypeProfile>> updateTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated TypeProfile", required = true)
            @Valid @RequestBody TypeProfile typeProfile) {
        // Check if the entity exists
        if (!typeProfileService.getTypeProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Get the existing entity to get its PID and internalId
        TypeProfile existing = typeProfileService.getTypeProfile(id).get();

        // Set the PID from the existing entity
        typeProfile.setInternalId(existing.getId());

        // Ensure internal ID is preserved
        typeProfile.setInternalId(existing.getInternalId());

        // Validate BEFORE saving
        ValidationResult validationResult = ruleService.executeRules(
                RuleTask.VALIDATE,
                typeProfile,
                ValidationResult::new
        );

        // Check if validation failed based on your validation policy
        if (hasValidationErrors(validationResult)) {
            throw new ValidationException("Entity validation failed", validationResult);
        }

        // Only save if validation passes
        TypeProfile updatedTypeProfile = typeProfileService.updateTypeProfile(typeProfile);
        EntityModel<TypeProfile> entityModel = typeProfileModelAssembler.toModel(updatedTypeProfile);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Delete a TypeProfile",
            description = "Deletes a TypeProfile entity",
            responses = {
                    @ApiResponse(responseCode = "204", description = "TypeProfile deleted"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<Void> deleteTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id) {
        if (!typeProfileService.getTypeProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        typeProfileService.deleteTypeProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PatchMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Partially update a TypeProfile",
            description = "Updates specific fields of an existing TypeProfile entity",
            responses = {
                    @ApiResponse(responseCode = "200", description = "TypeProfile patched",
                            content = @Content(mediaType = "application/hal+json",
                                    schema = @Schema(implementation = TypeProfile.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<EntityModel<TypeProfile>> patchTypeProfile(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @PathVariable String id,
            @Parameter(description = "Partial TypeProfile with fields to update", required = true)
            @RequestBody TypeProfile typeProfilePatch) {
        if (!typeProfileService.getTypeProfile(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // Validate the patch if it contains fields that need validation
        if (typeProfilePatch.getAttributes() != null || typeProfilePatch.getInheritsFrom() != null) {
            // Get the current entity
            TypeProfile existing = typeProfileService.getTypeProfile(id).get();

            // Create a merged entity for validation
            TypeProfile merged = new TypeProfile();
            merged.setInternalId(existing.getId());
            merged.setName(typeProfilePatch.getName() != null ? typeProfilePatch.getName() : existing.getName());
            merged.setDescription(typeProfilePatch.getDescription() != null ? typeProfilePatch.getDescription() : existing.getDescription());
            merged.setAttributes(typeProfilePatch.getAttributes() != null ? typeProfilePatch.getAttributes() : existing.getAttributes());
            merged.setInheritsFrom(typeProfilePatch.getInheritsFrom() != null ? typeProfilePatch.getInheritsFrom() : existing.getInheritsFrom());

            // Validate the merged entity
            ValidationResult validationResult = ruleService.executeRules(
                    RuleTask.VALIDATE,
                    merged,
                    ValidationResult::new
            );

            // Check if validation failed
            if (hasValidationErrors(validationResult)) {
                throw new ValidationException("Entity validation failed", validationResult);
            }
        }

        TypeProfile patchedTypeProfile = typeProfileService.patchTypeProfile(id, typeProfilePatch);
        EntityModel<TypeProfile> entityModel = typeProfileModelAssembler.toModel(patchedTypeProfile);
        return ResponseEntity.ok(entityModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @GetMapping("/{id}/inheritanceTree")
    @io.swagger.v3.oas.annotations.Operation(
            summary = "Get inheritance tree of a TypeProfile",
            description = "Returns the inheritance tree of a TypeProfile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Inheritance tree found",
                            content = @Content(mediaType = "application/hal+json")),
                    @ApiResponse(responseCode = "404", description = "TypeProfile not found")
            }
    )
    public ResponseEntity<EntityModel<TypeProfileInheritance>> getInheritanceTree(
            @Parameter(description = "PID or internal ID of the TypeProfile", required = true)
            @NotNull @PathVariable String id) {
        EntityModel<TypeProfileInheritance> resources = buildInheritanceTree(typeProfileService.getTypeProfile(id).orElseThrow());
        resources.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(id)).withSelfRel());
        resources.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(id)).withRel("typeProfile"));

        return ResponseEntity.ok(resources);
    }

    /**
     * Builds an inheritance tree for a TypeProfile.
     *
     * @param typeProfile the TypeProfile to build the inheritance tree for
     * @return an EntityModel containing the inheritance tree
     */
    private EntityModel<TypeProfileInheritance> buildInheritanceTree(TypeProfile typeProfile) {
        List<EntityModel<Attribute>> attributes = new ArrayList<>();
        typeProfile.getAttributes().forEach(profileAttribute -> {
            EntityModel<Attribute> attribute = EntityModel.of(profileAttribute);
            attribute.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(profileAttribute.getDataType().getId())).withRel("dataType"));
            attributes.add(attribute);
        });

        List<EntityModel<TypeProfileInheritance>> inheritsFrom = new ArrayList<>();
        typeProfile.getInheritsFrom().forEach(inheritedTypeProfile -> {
            inheritsFrom.add(buildInheritanceTree(inheritedTypeProfile));
        });

        EntityModel<TypeProfileInheritance> node = EntityModel.of(
                new TypeProfileInheritance(typeProfile.getId(),
                        typeProfile.getName(),
                        typeProfile.getDescription(),
                        CollectionModel.of(attributes),
                        CollectionModel.of(inheritsFrom)));

        // Add links
        node.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(typeProfile.getId())).withRel("inheritanceTree"));
        node.add(linkTo(methodOn(TypeProfileController.class).getTypeProfile(typeProfile.getId())).withRel("typeProfile"));
        node.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(typeProfile.getId())).withRel("inheritedAttributes"));
        node.add(linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(typeProfile.getId())).withRel("operations"));

        return node;
    }

    /**
     * Checks if a validation result contains errors based on the configured validation level.
     *
     * @param validationResult the validation result to check
     * @return true if the validation result contains errors, false otherwise
     */
    private boolean hasValidationErrors(ValidationResult validationResult) {
        return validationResult.getOutputMessages()
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel())
                        && !entry.getValue().isEmpty());
    }

    public record TypeProfileInheritance(
            String id,
            String name,
            String description,
            CollectionModel<EntityModel<Attribute>> attributes,
            CollectionModel<EntityModel<TypeProfileInheritance>> inheritsFrom) {
    }
}
