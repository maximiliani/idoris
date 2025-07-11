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

/**
 * Enumeration of events that can trigger rule execution.
 * These events represent different lifecycle phases or operations on domain elements.
 *
 * <p>Rules can be configured to execute in response to specific events using the
 * {@link Rule#executeOnEvent()} attribute.</p>
 */
public enum RuleEvent {
    /**
     * Triggered when a new element is being created in the system.
     * Rules that execute on this event typically validate or enhance newly created elements.
     */
    CREATE,

    /**
     * Triggered when an existing element is being updated.
     * Rules that execute on this event typically validate changes or maintain consistency.
     */
    UPDATE,

    /**
     * Triggered when an element is being deleted from the system.
     * Rules that execute on this event typically validate that deletion is allowed or perform cleanup.
     */
    DELETE,

    /**
     * Triggered when an element is being consumed by an external system or process.
     * Rules that execute on this event typically prepare the element for consumption.
     */
    CONSUME,

    /**
     * Triggered when an element is being explicitly validated.
     * Rules that execute on this event perform various validation checks.
     */
    VALIDATE,

    /**
     * Triggered when an element is being published to external systems.
     * Rules that execute on this event typically prepare the element for publication.
     */
    PUBLISH,

    /**
     * Catch-all for other events not covered by the more specific values.
     * Rules that execute on this event should be prepared to handle various contexts.
     */
    OTHERS
}
