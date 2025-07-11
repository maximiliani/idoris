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

/**
 * A specialized visitor for validating domain entities against defined rules.
 *
 * <p>This visitor traverses the domain model and delegates validation to the rule service.
 * For each visited element, it executes all applicable validation rules and accumulates
 * the results into a hierarchical validation report.</p>
 *
 * <p>The visitor uses caching to avoid redundant validation of the same elements and
 * can detect and handle cycles in the domain model graph.</p>
 *
 * <p>This implementation is a Spring component that can be injected into services that
 * need validation capabilities.</p>
 */

import edu.kit.datamanager.idoris.rules.logic.Visitor;

public class ValidationVisitor extends Visitor<ValidationResult> {
    public ValidationVisitor() {
        super(ValidationResult::new);
    }
}
