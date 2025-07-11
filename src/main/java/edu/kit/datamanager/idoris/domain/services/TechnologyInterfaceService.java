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
import edu.kit.datamanager.idoris.dao.ITechnologyInterfaceDao;
import edu.kit.datamanager.idoris.domain.entities.TechnologyInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing TechnologyInterface entities.
 * This service provides methods for creating, updating, and retrieving TechnologyInterface entities.
 * It publishes domain events when entities are created, updated, or deleted.
 */
@Service
@Slf4j
public class TechnologyInterfaceService {
    private final ITechnologyInterfaceDao technologyInterfaceDao;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new TechnologyInterfaceService with the given dependencies.
     *
     * @param technologyInterfaceDao the TechnologyInterface repository
     * @param eventPublisher         the event publisher service
     */
    public TechnologyInterfaceService(ITechnologyInterfaceDao technologyInterfaceDao, EventPublisherService eventPublisher) {
        this.technologyInterfaceDao = technologyInterfaceDao;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Creates a new TechnologyInterface entity.
     *
     * @param technologyInterface the TechnologyInterface entity to create
     * @return the created TechnologyInterface entity
     */
    @Transactional
    public TechnologyInterface createTechnologyInterface(TechnologyInterface technologyInterface) {
        log.debug("Creating TechnologyInterface: {}", technologyInterface);
        TechnologyInterface saved = technologyInterfaceDao.save(technologyInterface);
        eventPublisher.publishEntityCreated(saved);
        log.info("Created TechnologyInterface with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Updates an existing TechnologyInterface entity.
     *
     * @param technologyInterface the TechnologyInterface entity to update
     * @return the updated TechnologyInterface entity
     * @throws IllegalArgumentException if the TechnologyInterface does not exist
     */
    @Transactional
    public TechnologyInterface updateTechnologyInterface(TechnologyInterface technologyInterface) {
        log.debug("Updating TechnologyInterface: {}", technologyInterface);

        if (technologyInterface.getPid() == null || technologyInterface.getPid().isEmpty()) {
            throw new IllegalArgumentException("TechnologyInterface must have a PID to be updated");
        }

        // Get the current version before updating
        TechnologyInterface existing = technologyInterfaceDao.findById(technologyInterface.getPid())
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with PID: " + technologyInterface.getPid()));

        Long previousVersion = existing.getVersion();

        TechnologyInterface saved = technologyInterfaceDao.save(technologyInterface);
        eventPublisher.publishEntityUpdated(saved, previousVersion);
        log.info("Updated TechnologyInterface with PID: {}", saved.getPid());
        return saved;
    }

    /**
     * Deletes a TechnologyInterface entity.
     *
     * @param pid the PID of the TechnologyInterface to delete
     * @throws IllegalArgumentException if the TechnologyInterface does not exist
     */
    @Transactional
    public void deleteTechnologyInterface(String pid) {
        log.debug("Deleting TechnologyInterface with PID: {}", pid);

        TechnologyInterface technologyInterface = technologyInterfaceDao.findById(pid)
                .orElseThrow(() -> new IllegalArgumentException("TechnologyInterface not found with PID: " + pid));

        technologyInterfaceDao.delete(technologyInterface);
        eventPublisher.publishEntityDeleted(technologyInterface);
        log.info("Deleted TechnologyInterface with PID: {}", pid);
    }

    /**
     * Retrieves a TechnologyInterface entity by its PID.
     *
     * @param pid the PID of the TechnologyInterface to retrieve
     * @return an Optional containing the TechnologyInterface, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<TechnologyInterface> getTechnologyInterface(String pid) {
        log.debug("Retrieving TechnologyInterface with PID: {}", pid);
        return technologyInterfaceDao.findById(pid);
    }

    /**
     * Retrieves all TechnologyInterface entities.
     *
     * @return a list of all TechnologyInterface entities
     */
    @Transactional(readOnly = true)
    public List<TechnologyInterface> getAllTechnologyInterfaces() {
        log.debug("Retrieving all TechnologyInterfaces");
        return technologyInterfaceDao.findAll();
    }
}