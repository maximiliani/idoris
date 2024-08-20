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

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.validators.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("OperationTypeProfile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperationTypeProfile extends GenericIDORISEntity {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<OperationTypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private Set<Attribute> attributes;

    @Relationship(value = "outputs", direction = Relationship.Direction.OUTGOING)
    private Set<Attribute> outputs;

    @Relationship(value = "adapters", direction = Relationship.Direction.OUTGOING)
    private Set<String> adapters;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
