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

package edu.kit.datamanager.idoris.visitors;

import edu.kit.datamanager.idoris.domain.entities.*;
import edu.kit.datamanager.idoris.domain.relationships.AttributeReference;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;

public interface Visitor<T> {
    T visit(Attribute attribute, Object... args);

    T visit(AttributeMapping attributeMapping, Object... args);

    T visit(BasicDataType basicDataType, Object... args);

    T visit(TypeProfile typeProfile, Object... args);

    T visit(Operation operation, Object... args);

    T visit(OperationStep operationStep, Object... args);

    T visit(OperationTypeProfile operationTypeProfile, Object... args);

    T visit(AttributeReference attributeReference, Object... args);

    T visit(ProfileAttribute profileAttribute, Object... args);
}
