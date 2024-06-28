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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

@Node("Operation")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@Validated
public class Operation extends GenericIDORISEntity {

    @Relationship(value = "executableOn", direction = Relationship.Direction.INCOMING)
    @NotNull(message = "Please specify the data type on which the operation can be executed.")
    private AttributeReference executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.OUTGOING)
    private Set<AttributeReference> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.INCOMING)
    private Set<AttributeReference> environment;

    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    @NotNull(message = "Please specify the steps of the operation.")
    @Valid
//    @Min(value = 1, message = "Please specify at least one step for the operation.")
    private List<OperationStep> execution;

    @NotBlank(message = "For better human readability and understanding, please provide a name for the operation.")
    private String name;
    private String description;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
