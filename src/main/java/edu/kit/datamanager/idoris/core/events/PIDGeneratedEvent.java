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
import lombok.Getter;
import lombok.ToString;

/**
 * Event that is published when a PID is generated for an entity.
 * This event carries the entity and the generated PID, and can be used by listeners
 * to perform additional operations like PID record creation, indexing, etc.
 *
 * @param <T> the type of entity for which the PID was generated, must extend AdministrativeMetadata
 */
@Getter
@ToString(callSuper = true)
public class PIDGeneratedEvent<T extends AdministrativeMetadata> extends AbstractDomainEvent {
    private final T entity;
    private final String pid;
    private final boolean isNewPID;

    /**
     * Creates a new PIDGeneratedEvent for the given entity and PID.
     *
     * @param entity   the entity for which the PID was generated
     * @param pid      the generated PID
     * @param isNewPID indicates whether this is a newly generated PID or an existing one
     */
    public PIDGeneratedEvent(T entity, String pid, boolean isNewPID) {
        this.entity = entity;
        this.pid = pid;
        this.isNewPID = isNewPID;
    }

    /**
     * Creates a new PIDGeneratedEvent for the given entity and PID.
     * Assumes that the PID is newly generated.
     *
     * @param entity the entity for which the PID was generated
     * @param pid    the generated PID
     */
    public PIDGeneratedEvent(T entity, String pid) {
        this(entity, pid, true);
    }

    /**
     * Gets the entity for which the PID was generated.
     *
     * @return the entity
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Gets the generated PID.
     *
     * @return the PID
     */
    public String getPid() {
        return pid;
    }

    /**
     * Indicates whether this is a newly generated PID or an existing one.
     *
     * @return true if the PID was newly generated, false if it already existed
     */
    public boolean isNewPID() {
        return isNewPID;
    }
}
