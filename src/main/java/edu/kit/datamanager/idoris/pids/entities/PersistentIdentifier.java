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

package edu.kit.datamanager.idoris.pids.entities;

import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a Persistent Identifier (PID) in the system.
 * This is stored as a separate node in Neo4j and points to the entity it identifies.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Node("PersistentIdentifier")
public class PersistentIdentifier {

    /**
     * The PID value, which is also the primary key of this entity.
     */
    @Id
    private String pid;

    /**
     * The type of entity this PID identifies.
     */
    private String entityType;

    /**
     * The internal ID of the entity this PID identifies.
     */
    private String entityInternalId;

    /**
     * Flag indicating whether this PID record is a tombstone (entity has been deleted).
     */
    private boolean tombstone;

    /**
     * Timestamp when the entity was deleted (only set if tombstone is true).
     */
    private Instant deletedAt;

    /**
     * Version of this PID record.
     */
    @Version
    private Long version;

    /**
     * Timestamp when this PID record was created.
     */
    @CreatedDate
    private Instant createdAt;

    /**
     * Timestamp when this PID record was last modified.
     */
    @LastModifiedDate
    private Instant lastModifiedAt;

    /**
     * Relationship to the entity this PID identifies.
     * This is null if the entity has been deleted (tombstone is true).
     */
    @Relationship(value = "IDENTIFIES", direction = Relationship.Direction.OUTGOING)
    private AdministrativeMetadata entity;

    /**
     * Additional metadata stored in the PID record.
     * This is a map of key-value pairs that can be used to store any additional information.
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Adds a metadata entry to this PID record.
     *
     * @param key   The key of the metadata entry
     * @param value The value of the metadata entry
     * @return This PID record for method chaining
     */
    public PersistentIdentifier addMetadata(String key, String value) {
        metadata.put(key, value);
        return this;
    }

    /**
     * Removes a metadata entry from this PID record.
     *
     * @param key The key of the metadata entry to remove
     * @return This PID record for method chaining
     */
    public PersistentIdentifier removeMetadata(String key) {
        metadata.remove(key);
        return this;
    }

    /**
     * Clears all metadata entries from this PID record.
     *
     * @return This PID record for method chaining
     */
    public PersistentIdentifier clearMetadata() {
        metadata.clear();
        return this;
    }

    /**
     * Gets the value of a metadata entry.
     *
     * @param key The key of the metadata entry
     * @return The value of the metadata entry, or null if the key does not exist
     */
    public String getMetadataValue(String key) {
        return metadata.get(key);
    }

    /**
     * Marks this PID record as a tombstone, indicating that the entity it identifies has been deleted.
     *
     * @param deletedAt The timestamp when the entity was deleted
     * @return This PID record for method chaining
     */
    public PersistentIdentifier markAsTombstone(Instant deletedAt) {
        this.tombstone = true;
        this.deletedAt = deletedAt;
        this.entity = null;  // Remove the relationship to the entity
        return this;
    }
}
