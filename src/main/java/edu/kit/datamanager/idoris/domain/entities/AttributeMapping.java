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

import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;

@Node("AttributeMapping")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AttributeMapping extends VisitableElement implements Serializable {
    @Id
    @GeneratedValue
    private String id;
    private String name;

    @Relationship(value = "input", direction = Relationship.Direction.INCOMING)
    private Attribute input;

    private String value;
    private Integer index;

    @Relationship(value = "output", direction = Relationship.Direction.OUTGOING)
    private Attribute output;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
