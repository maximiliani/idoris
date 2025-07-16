/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.core.domain.dao;

import edu.kit.datamanager.idoris.core.domain.AdministrativeMetadata;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IGenericRepo<T extends AdministrativeMetadata> extends Neo4jRepository<T, String>, ListCrudRepository<T, String>, PagingAndSortingRepository<T, String> {
    // This interface serves as a marker for generic repositories.
    // It can be extended by specific repositories to inherit common methods.
    // Additional methods can be defined here if needed.

    T findByPid(String pid);
}