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

package edu.kit.datamanager.idoris.domain.services;

import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.dao.IAttributeDao;
import edu.kit.datamanager.idoris.domain.entities.Attribute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing Attribute entities.
 * This service provides methods for creating, updating, and retrieving Attribute entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
public class AttributeService {
    private final IAttributeDao attributeDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new AttributeService with the given dependencies.
     *
     * @param attributeDao   the Attribute repository
     * @param eventPublisher the event publisher service
     */
    public AttributeService(IAttributeDao attributeDao, EventPublisherService eventPublisher) {
        this.attributeDao = attributeDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new Attribute entity.
     *
     * @param attribute the Attribute entity to create
     * @return the created Attribute entity
     */
    @Transactional
    public Attribute createAttribute(Attribute attribute) {
        log.debug("Creating Attribute: {}", attribute);
        Attribute saved = attributeDao.save(attribute);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created Attribute with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Updates an existing Attribute entity.
     *
     * @param attribute the Attribute entity to update
     * @return the updated Attribute entity
     * @throws IllegalArgumentException if the Attribute does not exist
     */
    @Transactional
    public Attribute updateAttribute(Attribute attribute) {
        log.debug("Updating Attribute: {}", attribute);

        if (attribute.getPid() == null || attribute.getPid().isEmpty()) {
            throw new IllegalArgumentException("Attribute must have a PID to be updated");
        }

        // Get the current version before updating
        Attribute existing = attributeDao.findById(attribute.getPid())
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with PID: " + attribute.getPid()));

        Long previousVersion = existing.getVersion();

        Attribute saved = attributeDao.save(attribute);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated Attribute with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Deletes an Attribute entity.
     *
     * @param pid the PID of the Attribute to delete
     * @throws IllegalArgumentException if the Attribute does not exist
     */
    @Transactional
    public void deleteAttribute(String pid) {
        log.debug("Deleting Attribute with PID: {}", pid);

        Attribute attribute = attributeDao.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found with PID: " + pid));

        attributeDao.delete(attribute);
        eventPublisher.publishEntityDeleted(attribute);
        log.info("Deleted Attribute with PID: {}", pid);
    }

    /**
     * Retrieves an Attribute entity by its PID.
     *
     * @param pid the PID of the Attribute to retrieve
     * @return an Optional containing the Attribute, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Attribute> getAttribute(String pid) {
        log.debug("Retrieving Attribute with PID: {}", pid);
        return attributeDao.findById(pid);
    }

    /**
     * Retrieves all Attribute entities.
     *
     * @return a list of all Attribute entities
     */
    @Transactional(readOnly = true)
    public List<Attribute> getAllAttributes() {
        log.debug("Retrieving all Attributes");
        return attributeDao.findAll();
    }

    /**
     * Deletes orphaned Attribute entities.
     * An orphaned Attribute is one that has a dataType relationship but is not referenced by any other node.
     */
    @Transactional
    public void deleteOrphanedAttributes() {
        log.debug("Deleting orphaned Attributes");
        attributeDao.deleteOrphanedAttributes();
        log.info("Deleted orphaned Attributes");
    }
}