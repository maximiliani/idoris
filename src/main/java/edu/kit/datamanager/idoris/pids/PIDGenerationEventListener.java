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

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.events.EntityCreatedEvent;
import edu.kit.datamanager.idoris.core.events.EventPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event listener that generates PIDs for newly created entities.
 * This listener subscribes to EntityCreatedEvent and uses the TypedPIDMakerIDGenerator
 * to generate PIDs for entities that don't already have one.
 */
@Component
@Slf4j
public class PIDGenerationEventListener {
    private final TypedPIDMakerIDGenerator pidGenerator;
    private final EventPublisherService eventPublisher;

    /**
     * Creates a new PIDGenerationEventListener with the given dependencies.
     *
     * @param pidGenerator   the PID generator to use
     * @param eventPublisher the event publisher service
     */
    public PIDGenerationEventListener(TypedPIDMakerIDGenerator pidGenerator, EventPublisherService eventPublisher) {
        this.pidGenerator = pidGenerator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handles EntityCreatedEvent by generating a PID for the entity if it doesn't already have one.
     * This method is executed in a new transaction to ensure that the PID generation is isolated
     * from the transaction that created the entity.
     *
     * @param event the entity created event
     */
    @EventListener
    @Transactional
    public void handleEntityCreatedEvent(EntityCreatedEvent<AdministrativeMetadata> event) {
        AdministrativeMetadata entity = event.getEntity();
        log.debug("Handling EntityCreatedEvent for entity: {}", entity);

        if (entity.getPid() == null || entity.getPid().isEmpty()) {
            log.info("Generating PID for entity: {}", entity);
            String pid = pidGenerator.generateId(entity.getClass().getSimpleName(), entity);
            entity.setPid(pid);
            log.info("Generated PID: {} for entity: {}", pid, entity);

            // Publish a PID generated event
            eventPublisher.publishPIDGenerated(entity, pid);
        } else {
            log.debug("Entity already has a PID: {}", entity.getPid());
        }
    }
}
