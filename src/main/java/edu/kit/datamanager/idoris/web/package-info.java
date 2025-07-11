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
 * Web module for IDORIS.
 * This module is responsible for the web API and controllers.
 * It provides REST endpoints for accessing and manipulating entities.
 *
 * <p>The web module depends on the core module for base abstractions, the domain module for entity definitions,
 * and the domain.services package for business logic. It should not depend on repository or other infrastructure concerns directly.</p>
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "IDORIS Web",
        allowedDependencies = {"core", "domain", "domain.services"}
)
package edu.kit.datamanager.idoris.web;