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

package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.AttributeMapping;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

/**
 * Repository interface for AttributeMapping entities.
 */
public interface IAttributeMappingDao extends Neo4jRepository<AttributeMapping, String> {

    /**
     * Finds AttributeMapping entities by input attribute PID.
     *
     * @param pid the PID of the input attribute
     * @return an Iterable of AttributeMapping entities
     */
    @Query("MATCH (a:Attribute {pid: $pid})<-[:input]-(m:AttributeMapping) RETURN m")
    Iterable<AttributeMapping> findByInputAttributePid(String pid);

    /**
     * Finds AttributeMapping entities by output attribute PID.
     *
     * @param pid the PID of the output attribute
     * @return an Iterable of AttributeMapping entities
     */
    @Query("MATCH (a:Attribute {pid: $pid})<-[:output]-(m:AttributeMapping) RETURN m")
    Iterable<AttributeMapping> findByOutputAttributePid(String pid);
}
