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

package edu.kit.datamanager.idoris.visitors;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationResult {
    private List<ValidationMessage> messages = new ArrayList<>();
    private List<ValidationResult> children = new ArrayList<>();

//    public ValidationResult addMessage(String message, ValidationMessage.MessageSeverity type) {
//        messages.add(new ValidationMessage(message, type));
//        return this;
//    }

    public ValidationResult addMessage(String message, Object element, ValidationMessage.MessageSeverity type) {
        messages.add(new ValidationMessage(message, element, type));
        return this;
    }

    public ValidationResult addChild(ValidationResult child) {
        if (child != null && !child.isEmpty()) children.add(child);
        return this;
    }

    public boolean isValid() {
        boolean noErrorMessages = messages.stream().noneMatch(m -> m.getSeverity() == ValidationMessage.MessageSeverity.ERROR);
        boolean childrenValid = children.stream().allMatch(ValidationResult::isValid);
        return noErrorMessages && childrenValid;
    }

    public int getErrorCount() {
        int ownErrors = (int) messages.stream().filter(m -> m.getSeverity() == ValidationMessage.MessageSeverity.ERROR).count();
        int childErrors = children.stream().mapToInt(ValidationResult::getErrorCount).sum();
        return ownErrors + childErrors;
    }

    public int getWarningCount() {
        int ownWarnings = (int) messages.stream().filter(m -> m.getSeverity() == ValidationMessage.MessageSeverity.WARNING).count();
        int childWarnings = children.stream().mapToInt(ValidationResult::getWarningCount).sum();
        return ownWarnings + childWarnings;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "messages=" + messages +
                ", children=" + children +
                ", errorCount=" + getErrorCount() +
                ", warningCount=" + getWarningCount() +
                '}';
    }

    public boolean isEmpty() {
        boolean noErrorMessages = messages.isEmpty();
        boolean childrenEmpty = children.stream().allMatch(ValidationResult::isEmpty);
        boolean noErrors = getErrorCount() == 0;
        boolean noWarnings = getWarningCount() == 0;
        boolean noInfo = messages.stream().noneMatch(m -> m.getSeverity() == ValidationMessage.MessageSeverity.INFO);
        return noErrorMessages && childrenEmpty && noErrors && noWarnings;
    }

    @Getter
    public static final class ValidationMessage {
        private final String message;
        private final MessageSeverity severity;
        private final Object element;

//        public ValidationMessage(@NotEmpty String message, @NotNull ValidationResult.ValidationMessage.MessageSeverity severity) {
//            this(message, null, severity);
//        }

        public ValidationMessage(@NotEmpty String message, Object element, @NotNull ValidationResult.ValidationMessage.MessageSeverity severity) {
            this.message = message;
            this.severity = severity;
            this.element = element;
        }

        @Override
        public String toString() {
            return "ValidationMessage{" +
                    "message='" + message + '\'' +
                    ", type=" + severity +
                    ", element=" + element +
                    '}';
        }

        @AllArgsConstructor
        @Getter
        public enum MessageSeverity {
            INFO("INFO"),
            WARNING("WARNING"),
            ERROR("ERROR");
            private final String value;
        }
    }
}
