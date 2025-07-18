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

package edu.kit.datamanager.idoris.operations.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.operations.dao.IOperationDao;
import edu.kit.datamanager.idoris.operations.entities.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Operation entities.
 * This service provides methods for creating, updating, and retrieving Operation entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
public class OperationService {
    private final IOperationDao operationDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new OperationService with the given dependencies.
     *
     * @param operationDao   the Operation repository
     * @param eventPublisher the event publisher service
     */
    public OperationService(IOperationDao operationDao, EventPublisherService eventPublisher) {
        this.operationDao = operationDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Operation entity.
     *
     * @param operation the Operation entity to create
     * @return the created Operation entity
     */
    @Transactional
    public Operation createOperation(Operation operation) {
        log.debug("Creating Operation: {}", operation);
        Operation saved = operationDao.save(operation);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created Operation with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Updates an existing Operation entity.
     *
     * @param operation the Operation entity to update
     * @return the updated Operation entity
     * @throws IllegalArgumentException if the Operation does not exist
     */
    @Transactional
    public Operation updateOperation(Operation operation) {
        log.debug("Updating Operation: {}", operation);

        if (operation.getId() == null || operation.getId().isEmpty()) {
            throw new IllegalArgumentException("Operation must have a PID to be updated");
        }

        // Get the current version before updating
        Operation existing = operationDao.findById(operation.getId())
                .orElseThrow(() -> new IllegalArgumentException("Operation not found with PID: " + operation.getId()));

        Long previousVersion = existing.getVersion();

        Operation saved = operationDao.save(operation);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated Operation with PID: {}", saved.getId());
        return saved;
    }

    /**
     * Deletes an Operation entity.
     *
     * @param id the PID or internal ID of the Operation to delete
     * @throws IllegalArgumentException if the Operation does not exist
     */
    @Transactional
    public void deleteOperation(String id) {
        log.debug("Deleting Operation with ID: {}", id);

        Operation operation = operationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found with ID: " + id));

        operationDao.delete(operation);
        eventPublisher.publishEntityDeleted(operation);
        log.info("Deleted Operation with ID: {}", id);
    }

    /**
     * Retrieves an Operation entity by its PID or internal ID.
     *
     * @param id the PID or internal ID of the Operation to retrieve
     * @return an Optional containing the Operation, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Operation> getOperation(String id) {
        log.debug("Retrieving Operation with ID: {}", id);
        return operationDao.findById(id);
    }

    /**
     * Retrieves all Operation entities.
     *
     * @return a list of all Operation entities
     */
    @Transactional(readOnly = true)
    public List<Operation> getAllOperations() {
        log.debug("Retrieving all Operations");
        return operationDao.findAll();
    }

    /**
     * Retrieves all Operations for a DataType.
     *
     * @param dataTypeId the ID of the DataType (either PID or internal ID)
     * @return an iterable of Operations for the DataType
     */
    @Transactional(readOnly = true)
    public Iterable<Operation> getOperationsForDataType(String dataTypeId) {
        log.debug("Retrieving Operations for DataType with ID: {}", dataTypeId);
        return operationDao.getOperationsForDataType(dataTypeId);
    }

    /**
     * Partially updates an existing Operation entity.
     *
     * @param id             the PID or internal ID of the Operation to patch
     * @param operationPatch the partial Operation entity with fields to update
     * @return the patched Operation entity
     * @throws IllegalArgumentException if the Operation does not exist
     */
    @Transactional
    public Operation patchOperation(String id, Operation operationPatch) {
        log.debug("Patching Operation with ID: {}, patch: {}", id, operationPatch);
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Operation ID cannot be null or empty");
        }

        // Get the current entity
        Operation existing = operationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Operation not found with ID: " + id));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (operationPatch.getName() != null) {
            existing.setName(operationPatch.getName());
        }
        if (operationPatch.getDescription() != null) {
            existing.setDescription(operationPatch.getDescription());
        }
        if (operationPatch.getExecutableOn() != null) {
            existing.setExecutableOn(operationPatch.getExecutableOn());
        }
        if (operationPatch.getReturns() != null) {
            existing.setReturns(operationPatch.getReturns());
        }
        if (operationPatch.getEnvironment() != null) {
            existing.setEnvironment(operationPatch.getEnvironment());
        }
        if (operationPatch.getExecution() != null) {
            existing.setExecution(operationPatch.getExecution());
        }

        // Save the updated entity
        Operation saved = operationDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched Operation with PID: {}", saved.getId());
        return saved;
    }
}
