/*
 * Copyright (c) 2025 Karlsruhe Institute of Technology
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

package edu.kit.datamanager.idoris.rules.validation;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.entities.*;
import edu.kit.datamanager.idoris.rules.logic.Rule;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;

@Rule(
        appliesTo = {
                AtomicDataType.class,
                Attribute.class,
                AttributeMapping.class,
                Operation.class,
                OperationStep.class,
                TechnologyInterface.class,
                TypeProfile.class,
                GenericIDORISEntity.class
        },
        name = "SyntaxValidator",
        description = "Validates the syntax of various elements in the IDORIS system.",
        tasks = {RuleTask.VALIDATE}
)
public class SyntaxValidator {
}
