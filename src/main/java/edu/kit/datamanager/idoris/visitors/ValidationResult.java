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

    public void addMessage(String message, ValidationMessage.MessageType type) {
        messages.add(new ValidationMessage(message, type));
    }

    public void addMessage(String message, Object element, ValidationMessage.MessageType type) {
        messages.add(new ValidationMessage(message, element, type));
    }

    public void addChild(ValidationResult child) {
        if (child != null) children.add(child);
    }

    public boolean isValid() {
        boolean noErrorMessages = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.MessageType.ERROR);
        boolean childrenValid = children.stream().allMatch(ValidationResult::isValid);
        return noErrorMessages && childrenValid;
    }

    public int getErrorCount() {
        return (int) messages.stream().filter(m -> m.getType() == ValidationMessage.MessageType.ERROR).count();
    }

    public int getWarningCount() {
        return (int) messages.stream().filter(m -> m.getType() == ValidationMessage.MessageType.WARNING).count();
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

    @Getter
    public static final class ValidationMessage {
        private final String message;
        private final MessageType type;
        private final Object element;

        public ValidationMessage(@NotEmpty String message, @NotNull MessageType type) {
            this(message, null, type);
        }

        public ValidationMessage(@NotEmpty String message, Object element, @NotNull MessageType type) {
            this.message = message;
            this.type = type;
            this.element = element;
        }

        @Override
        public String toString() {
            return "ValidationMessage{" +
                    "message='" + message + '\'' +
                    ", type=" + type +
                    ", element=" + element +
                    '}';
        }

        @AllArgsConstructor
        @Getter
        public enum MessageType {
            INFO("INFO"),
            WARNING("WARNING"),
            ERROR("ERROR");
            private final String value;
        }
    }
}
