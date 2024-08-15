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

package edu.kit.datamanager.idoris.domain;

import edu.kit.datamanager.idoris.domain.entities.License;
import edu.kit.datamanager.idoris.domain.entities.User;
import edu.kit.datamanager.idoris.domain.relationships.StandardApplicability;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class GenericIDORISEntity extends VisitableElement implements Serializable {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    String pid;
    @Version
    Long version;
    @CreatedDate
    Instant createdAt;
    @LastModifiedDate
    Instant lastModifiedAt;
    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    Set<User> contributors;
    @Relationship(value = "license", direction = Relationship.Direction.OUTGOING)
    License license;
    @Relationship(value = "standards", direction = Relationship.Direction.OUTGOING)
    Set<StandardApplicability> standards;
    private String name;
    private String description;
}
