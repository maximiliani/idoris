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

import com.google.common.base.Ascii;
import edu.kit.datamanager.idoris.configuration.ApplicationProperties;
import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.entities.ORCiDUser;
import edu.kit.datamanager.idoris.pids.client.TypedPIDMakerClient;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecord;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecordEntry;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.neo4j.core.schema.IdGenerator;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * ID generator that uses the Typed PID Maker service to generate PIDs.
 * It also creates PID records with metadata from the GenericIDORISEntity.
 */
@Component
@Slf4j
@ConditionalOnBean(TypedPIDMakerConfig.class)
public class TypedPIDMakerIDGenerator implements IdGenerator<String> {
    private final TypedPIDMakerClient client;
    private final TypedPIDMakerConfig config;

    /**
     * The base URL for the Typed PID Maker service.
     * This is derived from the application properties.
     * Without a trailing slash to ensure proper URL construction.
     */
    private final String baseUrl;

    /**
     * Constructor.
     *
     * @param client The TypedPIDMakerClient
     * @param config The TypedPIDMakerConfig
     */
    @Autowired
    public TypedPIDMakerIDGenerator(ApplicationProperties applicationProperties, TypedPIDMakerClient client, TypedPIDMakerConfig config) {
        this.client = client;
        this.config = config;

        String tempBaseUrl = applicationProperties.getBaseUrl();
        // Validate the base URL from application properties
        if (tempBaseUrl == null || tempBaseUrl.trim().isEmpty()) {
            log.error("Base URL for Typed PID Maker service is not configured or is empty.");
            throw new IllegalArgumentException("Base URL for Typed PID Maker service must be configured.");
        }
        // Ensure the base URL does not end with a slash
        if (tempBaseUrl.endsWith("/")) {
            log.warn("Base URL for Typed PID Maker service should not end with a slash. Removing trailing slash.");
            tempBaseUrl = tempBaseUrl.substring(0, tempBaseUrl.length() - 1);
        }
        this.baseUrl = tempBaseUrl;
        log.info("Initialized TypedPIDMakerIDGenerator with base URL: {}", this.baseUrl);
    }

    @Override
    @Nonnull
    public String generateId(String primaryLabel, Object entity) {
        if (!(entity instanceof GenericIDORISEntity idorisEntity)) {
            log.warn("Entity is not a GenericIDORISEntity, falling back to UUID generation");
            return java.util.UUID.randomUUID().toString();
        }

        // If the entity already has a PID, check if we need to update the record
        if (idorisEntity.getPid() != null && !idorisEntity.getPid().isEmpty()) {
            if (config.isUpdatePIDRecords() && config.isMeaningfulPIDRecords()) {
                try {
                    PIDRecord existingRecord = client.getPIDRecord(idorisEntity.getPid());
                    PIDRecord updatedRecord = createPIDRecord(idorisEntity);
                    client.updatePIDRecord(existingRecord.pid(), updatedRecord);
                } catch (Exception e) {
                    log.error("Failed to update PID record for entity with PID {}: {}", idorisEntity.getPid(), e.getMessage());
                }
            }
            return idorisEntity.getPid();
        }

        // Create a new PID record
        try {
            log.debug("Creating PID record for entity with PID {}", idorisEntity.getPid());
            PIDRecord record = createPIDRecord(idorisEntity);
            PIDRecord createdRecord = client.createPIDRecord(record);
            log.info("Created new PID record with PID: {}", createdRecord.pid());

            // Update the entity with the new PID
            idorisEntity.setPid(createdRecord.pid());
            PIDRecord updatedRecord = createPIDRecord(idorisEntity);
            client.updatePIDRecord(createdRecord.pid(), updatedRecord);
            log.info("Updated entity with new digitalObjectLocation: {}", updatedRecord);
            return createdRecord.pid();
        } catch (Exception e) {
            log.error("Failed to create PID record: {}", e.getMessage());
            log.warn("Falling back to UUID generation");
            return java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * Creates a PID record with metadata from the GenericIDORISEntity.
     * Note: This method only adds metadata if the Helmholtz Kernel Information Profile allows it.
     *
     * @param entity The GenericIDORISEntity
     * @return The PID record
     */
    private PIDRecord createPIDRecord(GenericIDORISEntity entity) {
        List<PIDRecordEntry> recordEntries = new ArrayList<>();

        // Only add metadata if configured to do so
        if (config.isMeaningfulPIDRecords()) {
            // Helmholtz Kernel Information Profile
            recordEntries.add(new PIDRecordEntry("21.T11148/076759916209e5d62bd5", "21.T11148/b9b76f887845e32d29f7"));

//            // Add basic metadata
//            if (entity.getName() != null) {
//                recordEntries.add(new PIDRecordEntry("name", entity.getName()));
//            }
//
//            if (entity.getDescription() != null) {
//                recordEntries.add(new PIDRecordEntry("description", entity.getDescription()));
//            }

            // Add timestamps
            Instant createdAt = entity.getCreatedAt();
            if (createdAt != null) {
                recordEntries.add(new PIDRecordEntry("21.T11148/aafd5fb4c7222e2d950a", createdAt.toString()));
            }

            Instant lastModifiedAt = entity.getLastModifiedAt();
            if (lastModifiedAt != null) {
                recordEntries.add(new PIDRecordEntry("21.T11148/397d831aa3a9d18eb52c", lastModifiedAt.toString()));
            }

            // Add version information
            Long version = entity.getVersion();
            if (version != null) {
                recordEntries.add(new PIDRecordEntry("21.T11148/c692273deb2772da307f", version.toString()));
            }

//            // Add expected use cases
//            if (entity.getExpectedUseCases() != null && !entity.getExpectedUseCases().isEmpty()) {
//                recordEntries.add(new PIDRecordEntry("expectedUseCases", String.join(", ", entity.getExpectedUseCases())));
//            }

            // Add contributors
            if (entity.getContributors() != null && !entity.getContributors().isEmpty()) {
                entity.getContributors().forEach(contributor -> {
                    if (contributor instanceof ORCiDUser orcidUser) {
                        URL orcidURL = orcidUser.getOrcid();
                        if (orcidURL != null && !orcidURL.toString().isEmpty()) {
                            recordEntries.add(new PIDRecordEntry("21.T11148/1a73af9e7ae00182733b", orcidURL.toExternalForm()));
                            log.debug("Added ORCiD URL: {}", orcidURL);
                        } else {
                            log.warn("This ORCiDUser is invalid, skipping contributor entry: {}", orcidUser);
                        }
                    } else {
                        log.warn("This contributor does not have a URL, skipping: {}", contributor);
                    }
                });
            }

            // Add references
            if (entity.getReferences() != null && !entity.getReferences().isEmpty()) {
                entity.getReferences().forEach(reference -> {
                    String relationPID = reference.relationType();
                    String targetPID = reference.targetPID();
                    if (relationPID != null && !relationPID.isEmpty() && targetPID != null && !targetPID.isEmpty()) {
                        recordEntries.add(new PIDRecordEntry(relationPID, targetPID));
                        log.debug("Added reference: {} -> {}", relationPID, targetPID);
                    } else {
                        log.warn("Invalid reference found, skipping: {}", reference);
                    }
                });
            }


            String doLocation;
            if (entity.getPid() != null && !entity.getPid().isEmpty()) {
                doLocation = String.format("%s/pid/%s", baseUrl, entity.getPid());
            } else {
                log.warn("Entity PID is null or empty, creating temporary DO location with internal id.");
                String classname = Ascii.toLowerCase(entity.getClass().getSimpleName());
                doLocation = String.format("%s/api/%s/%s", baseUrl, classname, entity.getInternalId());
            }
            log.debug("Using DO location: {}", doLocation);
            recordEntries.add(new PIDRecordEntry("digitalObjectLocation", doLocation));
        }

        // Create the PID record
        PIDRecord pidRecord = new PIDRecord("", recordEntries);
        log.info("Created PIDRecord: {}", pidRecord);
        return pidRecord;
    }

}
