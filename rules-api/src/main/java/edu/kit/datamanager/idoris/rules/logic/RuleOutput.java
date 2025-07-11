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


public interface RuleOutput<T extends RuleOutput<T>> {

    /**
     * Merges the current output with others of the same type.
     * This method should be implemented to define how outputs are combined.
     *
     * @param others the other outputs to merge with
     * @return the merged output
     */
    T merge(T... others);

    /**
     * Generates an empty output of the same type.
     * This method should be implemented to create a new instance of the output type.
     *
     * @return a new empty instance of the same type
     */
    T empty();

    /**
     * Adds a message to the output with additional elements.
     *
     * @param message  the message to add
     * @param severity the severity of the message
     * @param elements additional elements related to the message
     * @return the updated output with the added message
     */
    default T addMessage(String message, OutputMessage.MessageSeverity severity, Object... elements) {
        return addMessage(new OutputMessage(message, severity, elements));
    }

    /**
     * Adds a message to the output.
     *
     * @param message the message to add
     * @return the updated output with the added message
     */
    T addMessage(OutputMessage message);
}