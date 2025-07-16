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

package edu.kit.datamanager.idoris.datatypes.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.datatypes.dao.IAtomicDataTypeDao;
import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing AtomicDataType entities.
 * This service provides methods for creating, updating, and retrieving AtomicDataType entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
public class AtomicDataTypeService {
    private final IAtomicDataTypeDao atomicDataTypeDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new AtomicDataTypeService with the given dependencies.
     *
     * @param atomicDataTypeDao the AtomicDataType repository
     * @param eventPublisher    the event publisher service
     */
    public AtomicDataTypeService(IAtomicDataTypeDao atomicDataTypeDao, EventPublisherService eventPublisher) {
        this.atomicDataTypeDao = atomicDataTypeDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new AtomicDataType entity.
     *
     * @param atomicDataType the AtomicDataType entity to create
     * @return the created AtomicDataType entity
     */
    @Transactional
    public AtomicDataType createAtomicDataType(AtomicDataType atomicDataType) {
        log.debug("Creating AtomicDataType: {}", atomicDataType);
        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created AtomicDataType with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Updates an existing AtomicDataType entity.
     *
     * @param atomicDataType the AtomicDataType entity to update
     * @return the updated AtomicDataType entity
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    public AtomicDataType updateAtomicDataType(AtomicDataType atomicDataType) {
        log.debug("Updating AtomicDataType: {}", atomicDataType);

        if (atomicDataType.getPid() == null || atomicDataType.getPid().isEmpty()) {
            throw new IllegalArgumentException("AtomicDataType must have a PID to be updated");
        }

        // Get the current version before updating
        AtomicDataType existing = atomicDataTypeDao.findById(atomicDataType.getPid())
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with PID: " + atomicDataType.getPid()));

        Long previousVersion = existing.getVersion();

        AtomicDataType saved = atomicDataTypeDao.save(atomicDataType);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated AtomicDataType with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Deletes an AtomicDataType entity.
     *
     * @param pid the PID of the AtomicDataType to delete
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    public void deleteAtomicDataType(String pid) {
        log.debug("Deleting AtomicDataType with PID: {}", pid);

        AtomicDataType atomicDataType = atomicDataTypeDao.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with PID: " + pid));

        atomicDataTypeDao.delete(atomicDataType);
        eventPublisher.publishEntityDeleted(atomicDataType);
        log.info("Deleted AtomicDataType with PID: {}", pid);
    }

    /**
     * Retrieves an AtomicDataType entity by its PID.
     *
     * @param pid the PID of the AtomicDataType to retrieve
     * @return an Optional containing the AtomicDataType, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<AtomicDataType> getAtomicDataType(String pid) {
        log.debug("Retrieving AtomicDataType with PID: {}", pid);
        return atomicDataTypeDao.findById(pid);
    }

    /**
     * Retrieves all AtomicDataType entities.
     *
     * @return a list of all AtomicDataType entities
     */
    @Transactional(readOnly = true)
    public List<AtomicDataType> getAllAtomicDataTypes() {
        log.debug("Retrieving all AtomicDataTypes");
        return atomicDataTypeDao.findAll();
    }

    /**
     * Partially updates an existing AtomicDataType entity.
     *
     * @param pid                 the PID of the AtomicDataType to patch
     * @param atomicDataTypePatch the partial AtomicDataType entity with fields to update
     * @return the patched AtomicDataType entity
     * @throws IllegalArgumentException if the AtomicDataType does not exist
     */
    @Transactional
    public AtomicDataType patchAtomicDataType(String pid, AtomicDataType atomicDataTypePatch) {
        log.debug("Patching AtomicDataType with PID: {}, patch: {}", pid, atomicDataTypePatch);
        if (pid == null || pid.isEmpty()) {
            throw new IllegalArgumentException("AtomicDataType PID cannot be null or empty");
        }

        // Get the current entity
        AtomicDataType existing = atomicDataTypeDao.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("AtomicDataType not found with PID: " + pid));
        Long previousVersion = existing.getVersion();

        // Apply non-null fields from the patch to the existing entity
        if (atomicDataTypePatch.getName() != null) {
            existing.setName(atomicDataTypePatch.getName());
        }
        if (atomicDataTypePatch.getDescription() != null) {
            existing.setDescription(atomicDataTypePatch.getDescription());
        }
        if (atomicDataTypePatch.getDefaultValue() != null) {
            existing.setDefaultValue(atomicDataTypePatch.getDefaultValue());
        }
        if (atomicDataTypePatch.getPrimitiveDataType() != null) {
            existing.setPrimitiveDataType(atomicDataTypePatch.getPrimitiveDataType());
        }
        if (atomicDataTypePatch.getRegularExpression() != null) {
            existing.setRegularExpression(atomicDataTypePatch.getRegularExpression());
        }
        if (atomicDataTypePatch.getPermittedValues() != null) {
            existing.setPermittedValues(atomicDataTypePatch.getPermittedValues());
        }
        if (atomicDataTypePatch.getForbiddenValues() != null) {
            existing.setForbiddenValues(atomicDataTypePatch.getForbiddenValues());
        }
        if (atomicDataTypePatch.getMinimum() != null) {
            existing.setMinimum(atomicDataTypePatch.getMinimum());
        }
        if (atomicDataTypePatch.getMaximum() != null) {
            existing.setMaximum(atomicDataTypePatch.getMaximum());
        }
        if (atomicDataTypePatch.getInheritsFrom() != null) {
            existing.setInheritsFrom(atomicDataTypePatch.getInheritsFrom());
        }

        // Save the updated entity
        AtomicDataType saved = atomicDataTypeDao.save(existing);

        // Publish the patched event
        eventPublisher.publishEntityPatched(saved, previousVersion);

        log.info("Patched AtomicDataType with PID: {}", saved.getPid());
        return saved;
    }
}
