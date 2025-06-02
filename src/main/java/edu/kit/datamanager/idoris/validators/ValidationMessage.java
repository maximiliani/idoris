/*
 * Copyright (c) 2024-2025 Karlsruhe Institute of Technology
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
import org.springframework.lang.Nullable;

public record ValidationMessage(@NotNull String message, @Nullable Object element, @NotNull MessageSeverity severity) {
    @AllArgsConstructor
    @Getter
    public enum MessageSeverity {
        INFO("INFO"),        // ordinal 0
        WARNING("WARNING"),  // ordinal 1
        ERROR("ERROR");      // ordinal 2
        private final String value;

        /**
         * Checks if this severity is higher than or equal to the other severity.
         * Assumes severities are ordered by their declaration (ordinal value):
         * INFO < WARNING < ERROR
         *
         * @param other The severity to compare against.
         * @return true if this severity is higher than or equal to other; false otherwise.
         */
        @SuppressWarnings("EnumOrdinal")
        public boolean isHigherOrEqualTo(MessageSeverity other) {
            if (other == null) {
                // Or throw new IllegalArgumentException("Cannot compare with a null severity");
                return false; // Or handle as per specific requirements for null inputs
            }
            return this.ordinal() >= other.ordinal();
        }
    }
}