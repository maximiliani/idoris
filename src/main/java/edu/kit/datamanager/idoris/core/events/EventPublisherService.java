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

package edu.kit.datamanager.idoris.core.events;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Service for publishing domain events.
 * This service wraps Spring's ApplicationEventPublisher to provide a more domain-specific API.
 */
@Service
@Slf4j
public class EventPublisherService {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new EventPublisherService with the given ApplicationEventPublisher.
     *
     * @param eventPublisher the Spring ApplicationEventPublisher
     */
    public EventPublisherService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes an entity created event.
     *
     * @param entity the newly created entity
     * @param <T>    the type of entity
     */
    public <T extends AdministrativeMetadata> void publishEntityCreated(T entity) {
        log.debug("Publishing EntityCreatedEvent for entity: {}", entity);
        eventPublisher.publishEvent(new EntityCreatedEvent<>(entity));
    }

    /**
     * Publishes an entity created event for non-AdministrativeMetadata entities.
     *
     * @param entity     the newly created entity
     * @param entityType the type identifier for the entity
     */
    public void publishEntityCreated(Object entity, String entityType) {
        log.debug("Publishing EntityCreatedEvent for entity: {}, type: {}", entity, entityType);
        eventPublisher.publishEvent(new GenericEntityCreatedEvent(entity, entityType));
    }

    /**
     * Publishes an entity updated event.
     *
     * @param entity          the updated entity
     * @param previousVersion the version of the entity before the update
     * @param <T>             the type of entity
     */
    public <T extends AdministrativeMetadata> void publishEntityUpdated(T entity, Long previousVersion) {
        log.debug("Publishing EntityUpdatedEvent for entity: {}, previous version: {}", entity, previousVersion);
        eventPublisher.publishEvent(new EntityUpdatedEvent<>(entity, previousVersion));
    }

    /**
     * Publishes an entity updated event for non-AdministrativeMetadata entities.
     *
     * @param entity     the updated entity
     * @param entityType the type identifier for the entity
     */
    public void publishEntityUpdated(Object entity, String entityType) {
        log.debug("Publishing EntityUpdatedEvent for entity: {}, type: {}", entity, entityType);
        eventPublisher.publishEvent(new GenericEntityUpdatedEvent(entity, entityType));
    }

    /**
     * Publishes an entity deleted event.
     *
     * @param entity the deleted entity
     * @param <T>    the type of entity
     */
    public <T extends AdministrativeMetadata> void publishEntityDeleted(T entity) {
        log.debug("Publishing EntityDeletedEvent for entity: {}", entity);
        eventPublisher.publishEvent(new EntityDeletedEvent<>(entity));
    }

    /**
     * Publishes an entity deleted event for non-AdministrativeMetadata entities.
     *
     * @param entity     the deleted entity
     * @param entityType the type identifier for the entity
     */
    public void publishEntityDeleted(Object entity, String entityType) {
        log.debug("Publishing EntityDeletedEvent for entity: {}, type: {}", entity, entityType);
        eventPublisher.publishEvent(new GenericEntityDeletedEvent(entity, entityType));
    }

    /**
     * Publishes a PID generated event.
     *
     * @param entity   the entity for which the PID was generated
     * @param pid      the generated PID
     * @param isNewPID indicates whether this is a newly generated PID or an existing one
     * @param <T>      the type of entity
     */
    public <T extends AdministrativeMetadata> void publishPIDGenerated(T entity, String pid, boolean isNewPID) {
        log.debug("Publishing PIDGeneratedEvent for entity: {}, PID: {}, isNewPID: {}", entity, pid, isNewPID);
        eventPublisher.publishEvent(new PIDGeneratedEvent<>(entity, pid, isNewPID));
    }

    /**
     * Publishes a PID generated event.
     * Assumes that the PID is newly generated.
     *
     * @param entity the entity for which the PID was generated
     * @param pid    the generated PID
     * @param <T>    the type of entity
     */
    public <T extends AdministrativeMetadata> void publishPIDGenerated(T entity, String pid) {
        publishPIDGenerated(entity, pid, true);
    }

    /**
     * Publishes an entity patched event.
     *
     * @param entity          the patched entity
     * @param previousVersion the version of the entity before the patch
     * @param <T>             the type of entity
     */
    public <T extends AdministrativeMetadata> void publishEntityPatched(T entity, Long previousVersion) {
        log.debug("Publishing EntityPatchedEvent for entity: {}, previous version: {}", entity, previousVersion);
        eventPublisher.publishEvent(new EntityPatchedEvent<>(entity, previousVersion));
    }

    /**
     * Publishes an entity patched event for non-AdministrativeMetadata entities.
     *
     * @param entity     the patched entity
     * @param entityType the type identifier for the entity
     */
    public void publishEntityPatched(Object entity, String entityType) {
        log.debug("Publishing EntityPatchedEvent for entity: {}, type: {}", entity, entityType);
        eventPublisher.publishEvent(new GenericEntityPatchedEvent(entity, entityType));
    }

    /**
     * Publishes a generic domain event.
     *
     * @param event the event to publish
     */
    public void publishEvent(DomainEvent event) {
        log.debug("Publishing event: {}", event);
        eventPublisher.publishEvent(event);
    }
}
