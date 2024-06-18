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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;

@Node("OperationStep")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class OperationStep extends VisitableElement implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private Integer executionOrderIndex;
    private String title;
    private ExecutionMode mode = ExecutionMode.sync;

    @Relationship(value = "steps", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> steps;

    @Relationship(value = "operation", direction = Relationship.Direction.OUTGOING)
    private Operation operation;

    @Relationship(value = "operationTypeProfile", direction = Relationship.Direction.OUTGOING)
    private OperationTypeProfile operationTypeProfile;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private List<AttributeMapping> attributes;

    @Relationship(value = "outputs", direction = Relationship.Direction.OUTGOING)
    private List<AttributeMapping> outputs;

    @Override
    protected void accept(Visitor<?> visitor, Object... args) {
        visitor.visit(this, args);
    }

    public enum ExecutionMode {
        sync,
        async
    }
}
