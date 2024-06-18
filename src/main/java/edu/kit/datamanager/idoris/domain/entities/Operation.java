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
import edu.kit.datamanager.idoris.domain.relationships.AttributeReference;
import edu.kit.datamanager.idoris.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.Set;

@Node("Operation")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Operation extends GenericIDORISEntity {

    @Relationship(value = "executableOn", direction = Relationship.Direction.OUTGOING)
    private AttributeReference executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.INCOMING)
    private Set<AttributeReference> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.OUTGOING)
    private Set<AttributeReference> environment;

    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> execution;

    private String name;
    private String description;

    @Override
    protected void accept(Visitor<?> visitor, Object... args) {
        visitor.visit(this, args);
    }
}
