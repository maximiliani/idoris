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

/**
 * Notification module for IDORIS.
 * This module is responsible for notifying subscribers about entity changes.
 * It provides a callback mechanism for external systems to be notified when entities are created, updated, or deleted.
 *
 * <p>The notification module depends on the core module for event infrastructure and the domain module for entity definitions.
 * It listens for entity lifecycle events and notifies subscribers.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "IDORIS Notification",
        allowedDependencies = {"core", "domain"}
)
package edu.kit.datamanager.idoris.notification;