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
import edu.kit.datamanager.idoris.domain.enums.Obligation;
import edu.kit.datamanager.idoris.validators.Visitor;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Node
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Attribute extends VisitableElement {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String pid;
    private String name;
    private String description;

    private boolean repeatable = false;
    private Obligation obligation = Obligation.Mandatory;
    private String value;

    @Relationship(value = "dataType", direction = Relationship.Direction.OUTGOING)
    @NotNull
    private DataType dataType;

    @Relationship(value = "override", direction = Relationship.Direction.OUTGOING)
    private Attribute override;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
