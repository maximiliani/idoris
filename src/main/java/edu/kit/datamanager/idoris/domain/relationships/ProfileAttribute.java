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

package edu.kit.datamanager.idoris.domain.relationships;

import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.domain.entities.DataType;
import edu.kit.datamanager.idoris.domain.enums.Obligation;
import edu.kit.datamanager.idoris.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.io.Serializable;

@RelationshipProperties
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ProfileAttribute extends VisitableElement implements Serializable {
    @RelationshipId
    private String id;

    private String name;
    private String description;
    private boolean repeatable = false;
    private Obligation obligation = Obligation.Mandatory;
    private String defaultValue;
    private String constantValue;

    @TargetNode
    private DataType dataType;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }
}
