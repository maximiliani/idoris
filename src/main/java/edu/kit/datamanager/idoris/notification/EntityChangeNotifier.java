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

package edu.kit.datamanager.idoris.notification;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.events.EntityCreatedEvent;
import edu.kit.datamanager.idoris.core.events.EntityDeletedEvent;
import edu.kit.datamanager.idoris.core.events.EntityUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Component that listens for entity change events and notifies subscribers.
 * This is a simple implementation of a callback system for entity changes.
 * In a real-world scenario, this would likely use a more sophisticated notification mechanism.
 */
@Component
@Slf4j
public class EntityChangeNotifier {

    // Map of entity type to set of subscribers for that type
    private final Map<String, Set<EntityChangeSubscriber>> typeSubscribers = new ConcurrentHashMap<>();

    // Map of entity PID to set of subscribers for that specific entity
    private final Map<String, Set<EntityChangeSubscriber>> entitySubscribers = new ConcurrentHashMap<>();

    /**
     * Subscribes to changes for a specific entity type.
     *
     * @param entityType the entity type to subscribe to
     * @param subscriber the subscriber to notify
     */
    public void subscribeToType(String entityType, EntityChangeSubscriber subscriber) {
        log.debug("Subscribing to changes for entity type: {}", entityType);
        typeSubscribers.computeIfAbsent(entityType, k -> new CopyOnWriteArraySet<>()).add(subscriber);
    }

    /**
     * Subscribes to changes for a specific entity.
     *
     * @param entityPid  the PID of the entity to subscribe to
     * @param subscriber the subscriber to notify
     */
    public void subscribeToEntity(String entityPid, EntityChangeSubscriber subscriber) {
        log.debug("Subscribing to changes for entity with PID: {}", entityPid);
        entitySubscribers.computeIfAbsent(entityPid, k -> new CopyOnWriteArraySet<>()).add(subscriber);
    }

    /**
     * Unsubscribes from changes for a specific entity type.
     *
     * @param entityType the entity type to unsubscribe from
     * @param subscriber the subscriber to remove
     */
    public void unsubscribeFromType(String entityType, EntityChangeSubscriber subscriber) {
        log.debug("Unsubscribing from changes for entity type: {}", entityType);
        Set<EntityChangeSubscriber> subscribers = typeSubscribers.get(entityType);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * Unsubscribes from changes for a specific entity.
     *
     * @param entityPid  the PID of the entity to unsubscribe from
     * @param subscriber the subscriber to remove
     */
    public void unsubscribeFromEntity(String entityPid, EntityChangeSubscriber subscriber) {
        log.debug("Unsubscribing from changes for entity with PID: {}", entityPid);
        Set<EntityChangeSubscriber> subscribers = entitySubscribers.get(entityPid);
        if (subscribers != null) {
            subscribers.remove(subscriber);
        }
    }

    /**
     * Handles entity created events.
     *
     * @param event the entity created event
     */
    @EventListener
    public void handleEntityCreated(EntityCreatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        String entityType = entity.getClass().getSimpleName();
        String entityPid = entity.getPid();

        log.debug("Handling EntityCreatedEvent for entity type: {}, PID: {}", entityType, entityPid);

        // Notify type subscribers
        Set<EntityChangeSubscriber> typeSubscribersSet = typeSubscribers.get(entityType);
        if (typeSubscribersSet != null) {
            for (EntityChangeSubscriber subscriber : typeSubscribersSet) {
                try {
                    subscriber.onEntityCreated(entity);
                } catch (Exception e) {
                    log.error("Error notifying subscriber for entity creation: {}", e.getMessage(), e);
                }
            }
        }

        // Notify entity subscribers (unlikely for creation, but included for completeness)
        // Skip if entityPid is null to avoid NullPointerException
        if (entityPid != null) {
            Set<EntityChangeSubscriber> entitySubscribersSet = entitySubscribers.get(entityPid);
            if (entitySubscribersSet != null) {
                for (EntityChangeSubscriber subscriber : entitySubscribersSet) {
                    try {
                        subscriber.onEntityCreated(entity);
                    } catch (Exception e) {
                        log.error("Error notifying subscriber for entity creation: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Handles entity updated events.
     *
     * @param event the entity updated event
     */
    @EventListener
    public void handleEntityUpdated(EntityUpdatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        String entityType = entity.getClass().getSimpleName();
        String entityPid = entity.getPid();

        log.debug("Handling EntityUpdatedEvent for entity type: {}, PID: {}", entityType, entityPid);

        // Notify type subscribers
        Set<EntityChangeSubscriber> typeSubscribersSet = typeSubscribers.get(entityType);
        if (typeSubscribersSet != null) {
            for (EntityChangeSubscriber subscriber : typeSubscribersSet) {
                try {
                    subscriber.onEntityUpdated(entity, event.getPreviousVersion());
                } catch (Exception e) {
                    log.error("Error notifying subscriber for entity update: {}", e.getMessage(), e);
                }
            }
        }

        // Notify entity subscribers
        // Skip if entityPid is null to avoid NullPointerException
        if (entityPid != null) {
            Set<EntityChangeSubscriber> entitySubscribersSet = entitySubscribers.get(entityPid);
            if (entitySubscribersSet != null) {
                for (EntityChangeSubscriber subscriber : entitySubscribersSet) {
                    try {
                        subscriber.onEntityUpdated(entity, event.getPreviousVersion());
                    } catch (Exception e) {
                        log.error("Error notifying subscriber for entity update: {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Handles entity deleted events.
     *
     * @param event the entity deleted event
     */
    @EventListener
    public void handleEntityDeleted(EntityDeletedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        String entityType = event.getEntityType();
        String entityPid = event.getEntityPid();

        log.debug("Handling EntityDeletedEvent for entity type: {}, PID: {}", entityType, entityPid);

        // Notify type subscribers
        Set<EntityChangeSubscriber> typeSubscribersSet = typeSubscribers.get(entityType);
        if (typeSubscribersSet != null) {
            for (EntityChangeSubscriber subscriber : typeSubscribersSet) {
                try {
                    subscriber.onEntityDeleted(entity);
                } catch (Exception e) {
                    log.error("Error notifying subscriber for entity deletion: {}", e.getMessage(), e);
                }
            }
        }

        // Notify entity subscribers
        // Skip if entityPid is null to avoid NullPointerException
        if (entityPid != null) {
            Set<EntityChangeSubscriber> entitySubscribersSet = entitySubscribers.get(entityPid);
            if (entitySubscribersSet != null) {
                for (EntityChangeSubscriber subscriber : entitySubscribersSet) {
                    try {
                        subscriber.onEntityDeleted(entity);
                    } catch (Exception e) {
                        log.error("Error notifying subscriber for entity deletion: {}", e.getMessage(), e);
                    }
                }

                // Remove subscribers for this entity since it no longer exists
                entitySubscribers.remove(entityPid);
            }
        }
    }
}
