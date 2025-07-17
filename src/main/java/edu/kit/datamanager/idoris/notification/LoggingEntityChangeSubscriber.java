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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A simple implementation of EntityChangeSubscriber that logs entity changes.
 * This class is provided as an example of how to implement the EntityChangeSubscriber interface.
 * In a real-world scenario, subscribers might send notifications via email, webhooks, or other channels.
 */
@Component
@Slf4j
public class LoggingEntityChangeSubscriber implements EntityChangeSubscriber {

    /**
     * Called when an entity is created.
     * Logs information about the created entity.
     *
     * @param entity the created entity
     */
    @Override
    public void onEntityCreated(AdministrativeMetadata entity) {
        log.info("Entity created: type={}, pid={}, name={}",
                entity.getClass().getSimpleName(),
                entity.getPid(),
                entity.getName());
    }

    /**
     * Called when an entity is updated.
     * Logs information about the updated entity and its previous version.
     *
     * @param entity          the updated entity
     * @param previousVersion the version of the entity before the update
     */
    @Override
    public void onEntityUpdated(AdministrativeMetadata entity, Long previousVersion) {
        log.info("Entity updated: type={}, pid={}, name={}, previousVersion={}, newVersion={}",
                entity.getClass().getSimpleName(),
                entity.getPid(),
                entity.getName(),
                previousVersion,
                entity.getVersion());
    }

    /**
     * Called when an entity is deleted.
     * Logs information about the deleted entity.
     *
     * @param entity the deleted entity
     */
    @Override
    public void onEntityDeleted(AdministrativeMetadata entity) {
        log.info("Entity deleted: type={}, pid={}, name={}",
                entity.getClass().getSimpleName(),
                entity.getPid(),
                entity.getName());
    }
}
