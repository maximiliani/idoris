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

package edu.kit.datamanager.idoris.pids;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.events.EntityCreatedEvent;
import edu.kit.datamanager.idoris.core.events.EntityDeletedEvent;
import edu.kit.datamanager.idoris.core.events.EntityUpdatedEvent;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Event listener that generates IDs for newly created entities.
 * This listener subscribes to EntityCreatedEvent and uses the PersistentIdentifierService
 * to create PersistentIdentifier entities for newly created AdministrativeMetadata entities.
 */
@Component
@Slf4j
public class MetadataEventListener {
    private final PersistentIdentifierService pidService;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new MetadataEventListener with the given dependencies.
     *
     * @param pidService     the PersistentIdentifierService
     * @param eventPublisher the event publisher service
     */
    public MetadataEventListener(PersistentIdentifierService pidService, EventPublisherService eventPublisher) {
        this.pidService = pidService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles EntityCreatedEvent by creating a PersistentIdentifier for the entity.
     * This method is executed in a new transaction to ensure that the ID creation is isolated
     * from the transaction that created the entity.
     *
     * @param event the entity created event
     */
    @EventListener(classes = {EntityCreatedEvent.class})
    @Transactional
    public void handleEntityCreatedEvent(EntityCreatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityCreatedEvent for entity: {}", entity);

        // Check if a PersistentIdentifier already exists for this entity
        Optional<PersistentIdentifier> existingPid = pidService.getPersistentIdentifier(entity);

        if (existingPid.isPresent()) {
            log.debug("Entity already has a PersistentIdentifier: {}", existingPid.get().getPid());
            return;
        }

        // Create a new PersistentIdentifier for the entity
        log.info("Creating PersistentIdentifier for entity: {}", entity);
        PersistentIdentifier pid = pidService.createPersistentIdentifier(entity);
        log.info("Created PersistentIdentifier with ID: {} for entity: {}", pid.getPid(), entity);

        // Publish an ID generated event
        eventPublisher.publishIDGenerated(entity, pid.getPid());
    }

    /**
     * Handles EntityUpdatedEvent by updating the PersistentIdentifier for the entity.
     * This method is executed in a new transaction to ensure that the ID update is isolated
     * from the transaction that updated the entity.
     *
     * @param event the entity updated event
     */
    @EventListener(classes = {EntityUpdatedEvent.class})
    @Transactional
    public void handleEntityUpdatedEvent(EntityUpdatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityUpdatedEvent for entity: {}", entity);

        // Check if a PersistentIdentifier exists for this entity
        Optional<PersistentIdentifier> existingPid = pidService.getPersistentIdentifier(entity);

        if (existingPid.isPresent()) {
            PersistentIdentifier pid = existingPid.get();
            log.info("Updating PersistentIdentifier for entity: {}", entity);
            pidService.updatePIDRecord(pid);
            log.info("Updated PersistentIdentifier with ID: {} for entity: {}", pid.getPid(), entity);
        } else {
            log.warn("No PersistentIdentifier found for entity, cannot update: {}", entity);
        }
    }


    /**
     * Handles EntityDeletedEvent by marking the PersistentIdentifier as a tombstone.
     * This method is executed in a new transaction to ensure that the tombstone creation is isolated
     * from the transaction that deleted the entity.
     *
     * @param event the entity deleted event
     */
    @EventListener(classes = {EntityDeletedEvent.class})
    @Transactional
    public void handleEntityDeletedEvent(EntityDeletedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityDeletedEvent for entity: {}", entity);

        // Mark the PersistentIdentifier as a tombstone
        Optional<PersistentIdentifier> optionalPid = pidService.markAsTombstone(entity);

        if (optionalPid.isPresent()) {
            PersistentIdentifier pid = optionalPid.get();
            log.info("Created tombstone for entity with ID: {}", pid.getPid());
        } else {
            log.warn("No PersistentIdentifier found for entity, cannot create tombstone: {}", entity);
        }
    }
}
