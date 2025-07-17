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

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;

/**
 * Interface for subscribers that want to be notified of entity changes.
 * Implementations of this interface can be registered with the EntityChangeNotifier
 * to receive callbacks when entities are created, updated, or deleted.
 */
public interface EntityChangeSubscriber {

    /**
     * Called when an entity is created.
     *
     * @param entity the created entity
     */
    void onEntityCreated(AdministrativeMetadata entity);

    /**
     * Called when an entity is updated.
     *
     * @param entity          the updated entity
     * @param previousVersion the version of the entity before the update
     */
    void onEntityUpdated(AdministrativeMetadata entity, Long previousVersion);

    /**
     * Called when an entity is deleted.
     *
     * @param entity the deleted entity
     */
    void onEntityDeleted(AdministrativeMetadata entity);
}
