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

import edu.kit.datamanager.idoris.domain.enums.Category;
import edu.kit.datamanager.idoris.domain.enums.PrimitiveDataTypes;
import edu.kit.datamanager.idoris.visitors.Visitor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("BasicDataType")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class BasicDataType extends DataType {
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private BasicDataType inheritsFrom;

    private PrimitiveDataTypes primitiveDataType;
    private Category category = Category.Format;
    private String unitName;
    private String unitSymbol;
    private String definedBy;
    private String standard_uncertainty;

    private String restrictions;
    private String regex;
    // TODO: Make this an enum
    private String regexFlavour = "ecma-262-RegExp";

    @Property("enum")
    private Set<String> valueEnum;

    @Override
    protected <T> T accept(Visitor<T> visitor, Object... args) {
        return visitor.visit(this, args);
    }

    @Override
    public boolean inheritsFrom(DataType dataType) {
        if (dataType instanceof BasicDataType basicDataType) {
            if (basicDataType.equals(this)) {
                return true;
            }
            return inheritsFrom != null && inheritsFrom.inheritsFrom(basicDataType);
        }
        return false;
    }

}
