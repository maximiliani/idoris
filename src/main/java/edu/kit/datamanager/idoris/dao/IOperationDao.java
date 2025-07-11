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

package edu.kit.datamanager.idoris.dao;

import edu.kit.datamanager.idoris.domain.entities.Operation;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "operations", path = "operations")
public interface IOperationDao extends IGenericRepo<Operation> {
    //    @Query("optional MATCH (:DataType {pid: $pid})-[:attributes|inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o")
    @Query("""
            MATCH (d:DataType {pid: $pid})<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:attributes]->(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o
            UNION
            MATCH (d:DataType {pid: $pid})-[:inheritsFrom*]->(:DataType)-[:attributes]->(:Attribute)-[:dataType]->(:DataType)-[:inheritsFrom*]->(:DataType)<-[:dataType]-(:Attribute)<-[:executableOn]-(o:Operation) RETURN o""")
    Iterable<Operation> getOperationsForDataType(String pid);
}
