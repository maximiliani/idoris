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

package edu.kit.datamanager.idoris.validators;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;

public record ValidationMessage(@NotNull String message, @Nullable Object element, @NotNull MessageSeverity severity) {
    @AllArgsConstructor
    @Getter
    public enum MessageSeverity {
        INFO("INFO"),
        WARNING("WARNING"),
        ERROR("ERROR");
        private final String value;

        public boolean isHigherOrEqualTo(MessageSeverity other) {
            return this.ordinal() >= other.ordinal();
        }
    }
}
