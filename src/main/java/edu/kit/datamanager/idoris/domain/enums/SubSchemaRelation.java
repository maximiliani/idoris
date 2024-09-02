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

package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SubSchemaRelation {
    denyAdditionalProperties("denyAdditionalProperties", "Implementing FDOs or inheriting TypeProfiles MUST NOT have additional properties to those defined in this TypeProfile."),
    allowAdditionalProperties("allowAdditionalProperties", "Implementing FDOs or inheriting TypeProfiles MAY have additional properties to those defined in this TypeProfile."),
    requireAllProperties("requireAllProperties", "Implementing FDOs or inheriting TypeProfiles MUST specify all properties defined in this TypeProfile."),
    requireAnyOfProperties("requireAnyOfProperties", "Implementing FDOs or inheriting TypeProfiles MUST specify at least one of the properties defined in this TypeProfile."),
    requireOneOfProperties("requireOneOfProperties", "Implementing FDOs or inheriting TypeProfiles MUST specify exactly one of the properties defined in this TypeProfile."),
    requireNoneOfProperties("requireNoneOfProperties", "Implementing FDOs or inheriting TypeProfiles MUST NOT specify any of the properties defined in this TypeProfile.");
    private final String name;
    private final String description;
}
