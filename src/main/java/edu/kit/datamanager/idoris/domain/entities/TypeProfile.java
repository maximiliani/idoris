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

package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.enums.SubSchemaRelation;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("TypeProfile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeProfile extends DataType {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<TypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.OUTGOING)
    private Set<ProfileAttribute> attributes;

    private String restrictions;
    private SubSchemaRelation subSchemaRelation = SubSchemaRelation.allowAdditionalProperties;
}
