/*
 * Copyright (c) 2024 Karlsruhe Institute of Technology
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

import edu.kit.datamanager.idoris.domain.entities.BasicDataType;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(collectionResourceRel = "basicDataTypes", path = "basicDataTypes")
public interface IBasicDataTypeDao extends IAbstractRepo<BasicDataType, String> {
    @RestResource(exported = false)
    @Query("MATCH (d:BasicDataType {pid: $pid})-[:inheritsFrom*]->(d2:BasicDataType) RETURN d2")
    Iterable<BasicDataType> findAllInInheritanceChain(String pid);
}
