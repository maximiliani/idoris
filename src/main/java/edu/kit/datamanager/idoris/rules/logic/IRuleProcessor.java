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

package edu.kit.datamanager.idoris.rules.logic;

import edu.kit.datamanager.idoris.domain.VisitableElement;

/**
 * Interface for rule processors that can be used to process visitable elements.
 * Each rule processor is responsible for a specific task and element type.
 *
 * @param <T> the type of visitable element this processor can handle
 * @param <R> the type of rule output produced by this processor
 */
public interface IRuleProcessor<T extends VisitableElement, R extends RuleOutput<R>> {

    /**
     * Process the given input element and update the provided output.
     *
     * @param input  the element to process
     * @param output the output to update with processing results
     */
    void process(T input, R output);
}
