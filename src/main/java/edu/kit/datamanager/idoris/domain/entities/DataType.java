/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;

@Node("DataType")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AtomicDataType.class, name = "AtomicDataType"),
        @JsonSubTypes.Type(value = TypeProfile.class, name = "TypeProfile"),
})
public abstract sealed class DataType extends GenericIDORISEntity permits AtomicDataType, TypeProfile {
    private TYPES type;

    private String defaultValue;

    public abstract boolean inheritsFrom(DataType dataType);

    public enum TYPES {
        AtomicDataType,
        TypeProfile
    }
}
