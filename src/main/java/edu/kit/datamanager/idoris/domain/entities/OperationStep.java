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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;

@Node("OperationStep")
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Validated
public class OperationStep extends VisitableElement implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @NotNull(message = "Execution order index must not be specified otherwise the order of steps is random.")
    @PositiveOrZero
    private Integer executionOrderIndex;

    @NotBlank(message = "For better human readability, please provide a title for the operation step.")
    private String title;

    @NotNull(message = "An execution mode must be specified. Default is synchronous execution.")
    private ExecutionMode mode = ExecutionMode.sync;

    @Relationship(value = "steps", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> steps;

    @Relationship(value = "operation", direction = Relationship.Direction.OUTGOING)
    private Operation operation;

    @Relationship(value = "operationTypeProfile", direction = Relationship.Direction.OUTGOING)
    private OperationTypeProfile operationTypeProfile;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private List<AttributeMapping> attributes;

    @Relationship(value = "output", direction = Relationship.Direction.OUTGOING)
    private List<AttributeMapping> output;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }

    @Override
    public String toString() {
        return "OperationStep{" +
                "id=" + id +
                ", executionOrderIndex=" + executionOrderIndex +
                ", title='" + title + '\'' +
                ", mode=" + mode +
                ", steps=" + steps +
                ", operation=" + operation +
                ", operationTypeProfile=" + operationTypeProfile +
                ", attributes=" + attributes +
                ", output=" + output +
                "} " + super.toString();
    }

    public enum ExecutionMode {
        sync,
        async
    }
}
