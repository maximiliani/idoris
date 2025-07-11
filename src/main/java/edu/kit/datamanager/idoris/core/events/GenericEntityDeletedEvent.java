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

import lombok.Getter;
import lombok.ToString;

/**
 * Event that is published when an entity is deleted from the system.
 * This event is for entities that don't extend GenericIDORISEntity.
 */
@Getter
@ToString(callSuper = true)
public class GenericEntityDeletedEvent extends AbstractDomainEvent {
    private final Object entity;
    private final String entityType;

    /**
     * Creates a new GenericEntityDeletedEvent for the given entity.
     *
     * @param entity     the deleted entity
     * @param entityType the type identifier for the entity
     */
    public GenericEntityDeletedEvent(Object entity, String entityType) {
        this.entity = entity;
        this.entityType = entityType;
    }

    /**
     * Gets the entity that was deleted.
     *
     * @return the deleted entity
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * Gets the type identifier of the entity.
     *
     * @return the entity type
     */
    public String getEntityType() {
        return entityType;
    }
}
