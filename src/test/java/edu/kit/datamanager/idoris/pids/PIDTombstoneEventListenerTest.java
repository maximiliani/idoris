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

import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import edu.kit.datamanager.idoris.core.domain.entities.AdministrativeMetadata;
import edu.kit.datamanager.idoris.core.events.EntityDeletedEvent;
import edu.kit.datamanager.idoris.pids.client.TypedPIDMakerClient;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecord;
import edu.kit.datamanager.idoris.pids.client.model.PIDRecordEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PIDTombstoneEventListenerTest {

    @Mock
    private TypedPIDMakerClient client;

    @Mock
    private TypedPIDMakerConfig config;

    private PIDTombstoneEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new PIDTombstoneEventListener(client, config);
    }

    @Test
    void handleEntityDeletedEvent_withValidPid_createsTombstone() {
        // Arrange
        String pid = "test-pid";
        TestEntity entity = new TestEntity();
        entity.setPid(pid);
        entity.setName("Test Entity");
        entity.setDescription("Test Description");
        entity.setCreatedAt(Instant.now());
        entity.setLastModifiedAt(Instant.now());
        entity.setVersion(1L);

        EntityDeletedEvent<AdministrativeMetadata> event = new EntityDeletedEvent<>(entity);

        PIDRecord existingRecord = new PIDRecord(pid, List.of(
                new PIDRecordEntry("digitalObjectLocation", "http://example.com/pid/" + pid),
                new PIDRecordEntry("name", "Test Entity")
        ));

        when(client.getPIDRecord(pid)).thenReturn(existingRecord);
        when(config.isMeaningfulPIDRecords()).thenReturn(true);

        // Act
        listener.handleEntityDeletedEvent(event);

        // Assert
        ArgumentCaptor<PIDRecord> recordCaptor = ArgumentCaptor.forClass(PIDRecord.class);
        verify(client).updatePIDRecord(eq(pid), recordCaptor.capture());

        PIDRecord updatedRecord = recordCaptor.getValue();
        assertEquals(pid, updatedRecord.pid());

        // Verify tombstone marker
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("tombstone") && entry.value().equals("true")));

        // Verify deletedAt timestamp
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("deletedAt")));

        // Verify entity type
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("entityType") && entry.value().equals("TestEntity")));

        // Verify Helmholtz Kernel Information Profile
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/076759916209e5d62bd5") &&
                        entry.value().equals("21.T11148/b9b76f887845e32d29f7")));

        // Verify basic metadata
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("name") && entry.value().equals("Test Entity")));
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("description") && entry.value().equals("Test Description")));
    }

    @Test
    void handleEntityDeletedEvent_withoutMeaningfulRecords_createsTombstoneWithMinimalInfo() {
        // Arrange
        String pid = "test-pid";
        TestEntity entity = new TestEntity();
        entity.setPid(pid);
        entity.setName("Test Entity");
        entity.setDescription("Test Description");

        EntityDeletedEvent<AdministrativeMetadata> event = new EntityDeletedEvent<>(entity);

        PIDRecord existingRecord = new PIDRecord(pid, List.of(
                new PIDRecordEntry("digitalObjectLocation", "http://example.com/pid/" + pid)
        ));

        when(client.getPIDRecord(pid)).thenReturn(existingRecord);
        when(config.isMeaningfulPIDRecords()).thenReturn(false);

        // Act
        listener.handleEntityDeletedEvent(event);

        // Assert
        ArgumentCaptor<PIDRecord> recordCaptor = ArgumentCaptor.forClass(PIDRecord.class);
        verify(client).updatePIDRecord(eq(pid), recordCaptor.capture());

        PIDRecord updatedRecord = recordCaptor.getValue();
        assertEquals(pid, updatedRecord.pid());

        // Verify tombstone marker
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("tombstone") && entry.value().equals("true")));

        // Verify deletedAt timestamp
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("deletedAt")));

        // Verify entity type
        assertTrue(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("entityType") && entry.value().equals("TestEntity")));

        // Verify no Helmholtz Kernel Information Profile
        assertFalse(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("21.T11148/076759916209e5d62bd5")));

        // Verify no basic metadata
        assertFalse(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("name")));
        assertFalse(updatedRecord.record().stream()
                .anyMatch(entry -> entry.key().equals("description")));
    }

    @Test
    void handleEntityDeletedEvent_withNullPid_doesNothing() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setPid(null);

        EntityDeletedEvent<AdministrativeMetadata> event = new EntityDeletedEvent<>(entity);

        // Act
        listener.handleEntityDeletedEvent(event);

        // Assert
        verify(client, never()).getPIDRecord(any());
        verify(client, never()).updatePIDRecord(any(), any());
    }

    @Test
    void handleEntityDeletedEvent_withEmptyPid_doesNothing() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setPid("");

        EntityDeletedEvent<AdministrativeMetadata> event = new EntityDeletedEvent<>(entity);

        // Act
        listener.handleEntityDeletedEvent(event);

        // Assert
        verify(client, never()).getPIDRecord(any());
        verify(client, never()).updatePIDRecord(any(), any());
    }

    @Test
    void handleEntityDeletedEvent_whenClientThrowsException_handlesGracefully() {
        // Arrange
        String pid = "test-pid";
        TestEntity entity = new TestEntity();
        entity.setPid(pid);

        EntityDeletedEvent<AdministrativeMetadata> event = new EntityDeletedEvent<>(entity);

        when(client.getPIDRecord(pid)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertDoesNotThrow(() -> listener.handleEntityDeletedEvent(event));
        verify(client, never()).updatePIDRecord(any(), any());
    }

    // Test entity class
    private static class TestEntity extends AdministrativeMetadata {
        @Override
        protected <T extends edu.kit.datamanager.idoris.rules.logic.RuleOutput<T>> T accept(
                edu.kit.datamanager.idoris.rules.logic.Visitor<T> visitor, Object... args) {
            // Simple implementation for testing purposes
            // Since this is just a test class, we return null
            return null;
        }
    }
}
