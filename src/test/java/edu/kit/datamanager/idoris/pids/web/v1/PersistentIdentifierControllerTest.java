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

package edu.kit.datamanager.idoris.pids.web.v1;

import edu.kit.datamanager.idoris.pids.entities.PersistentIdentifier;
import edu.kit.datamanager.idoris.pids.services.PersistentIdentifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PersistentIdentifierControllerTest {

    @Mock
    private PersistentIdentifierService service;

    @InjectMocks
    private PersistentIdentifierController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getAllPersistentIdentifiers_shouldReturnAllPersistentIdentifiers() throws Exception {
        // Arrange
        PersistentIdentifier pid1 = createPersistentIdentifier("pid1", "TestEntity", "entity1", false);
        PersistentIdentifier pid2 = createPersistentIdentifier("pid2", "TestEntity", "entity2", false);
        List<PersistentIdentifier> pids = List.of(pid1, pid2);

        when(service.getAllPersistentIdentifiers()).thenReturn(pids);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pids")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pid", is("pid1")))
                .andExpect(jsonPath("$[1].pid", is("pid2")));
    }

    @Test
    void getPersistentIdentifier_withExistingPid_shouldReturnPersistentIdentifier() throws Exception {
        // Arrange
        String pid = "test-pid";
        PersistentIdentifier persistentIdentifier = createPersistentIdentifier(pid, "TestEntity", "entity1", false);

        when(service.getPersistentIdentifier(pid)).thenReturn(Optional.of(persistentIdentifier));

        // Act & Assert
        mockMvc.perform(get("/api/v1/pids/{pid}", pid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pid", is(pid)))
                .andExpect(jsonPath("$.entityType", is("TestEntity")))
                .andExpect(jsonPath("$.entityInternalId", is("entity1")))
                .andExpect(jsonPath("$.tombstone", is(false)));
    }

    @Test
    void getPersistentIdentifier_withNonExistingPid_shouldReturnNotFound() throws Exception {
        // Arrange
        String pid = "non-existing-pid";

        when(service.getPersistentIdentifier(pid)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/pids/{pid}", pid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPersistentIdentifiersByEntityType_shouldReturnPersistentIdentifiersForEntityType() throws Exception {
        // Arrange
        String entityType = "TestEntity";
        PersistentIdentifier pid1 = createPersistentIdentifier("pid1", entityType, "entity1", false);
        PersistentIdentifier pid2 = createPersistentIdentifier("pid2", entityType, "entity2", false);
        List<PersistentIdentifier> pids = List.of(pid1, pid2);

        when(service.getPersistentIdentifiersByEntityType(entityType)).thenReturn(pids);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pids/byEntityType/{entityType}", entityType)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pid", is("pid1")))
                .andExpect(jsonPath("$[1].pid", is("pid2")))
                .andExpect(jsonPath("$[0].entityType", is(entityType)))
                .andExpect(jsonPath("$[1].entityType", is(entityType)));
    }

    @Test
    void getTombstones_shouldReturnTombstones() throws Exception {
        // Arrange
        PersistentIdentifier pid1 = createPersistentIdentifier("pid1", "TestEntity", "entity1", true);
        PersistentIdentifier pid2 = createPersistentIdentifier("pid2", "TestEntity", "entity2", true);
        List<PersistentIdentifier> pids = List.of(pid1, pid2);

        when(service.getTombstones()).thenReturn(pids);

        // Act & Assert
        mockMvc.perform(get("/api/v1/pids/tombstones")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].pid", is("pid1")))
                .andExpect(jsonPath("$[1].pid", is("pid2")))
                .andExpect(jsonPath("$[0].tombstone", is(true)))
                .andExpect(jsonPath("$[1].tombstone", is(true)));
    }

    private PersistentIdentifier createPersistentIdentifier(String pid, String entityType, String entityInternalId, boolean tombstone) {
        PersistentIdentifier persistentIdentifier = new PersistentIdentifier();
        persistentIdentifier.setPid(pid);
        persistentIdentifier.setEntityType(entityType);
        persistentIdentifier.setEntityInternalId(entityInternalId);
        persistentIdentifier.setTombstone(tombstone);
        if (tombstone) {
            persistentIdentifier.setDeletedAt(Instant.now());
        }
        persistentIdentifier.setCreatedAt(Instant.now());
        persistentIdentifier.setLastModifiedAt(Instant.now());
        persistentIdentifier.setVersion(1L);
        persistentIdentifier.setMetadata(new HashMap<>());
        return persistentIdentifier;
    }
}