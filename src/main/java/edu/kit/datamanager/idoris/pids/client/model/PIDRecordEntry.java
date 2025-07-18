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

package edu.kit.datamanager.idoris.pids.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a key-value pair in a SimplePidRecord.
 * This follows the PIDRecordEntry structure from the Typed PID Maker API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PIDRecordEntry(String key, String value) {
}
