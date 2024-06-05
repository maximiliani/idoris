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

import edu.kit.datamanager.idoris.domain.entities.Attribute;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RepositoryRestResource(collectionResourceRel = "attributes", path = "attributes")
public interface IAttributeDao extends IAbstractRepo<Attribute, String> {
    @Query("MATCH (n:Attribute)" +
            " WHERE size([(n)-[:dataType]->() | 1]) = 1 AND NOT (n)<-[]-()" +
            " WITH n" +
            " MATCH (x)-[r]->()" +
            " WHERE type(r) <> \"dataType\"" +
            " WITH collect(DISTINCT x) as otherNodes, n" +
            " WHERE NOT n IN otherNodes" +
            " DETACH DELETE n")
    @RestResource(exported = false)
    void deleteOrphanedAttributes();
}
