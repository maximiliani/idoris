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

package edu.kit.datamanager.idoris.rules.validation;

import edu.kit.datamanager.idoris.rules.logic.RuleOutput;
import edu.kit.datamanager.idoris.validators.ValidationMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValidationResult implements RuleOutput<ValidationResult> {
    private List<ValidationMessage> messages = new ArrayList<>();
    private List<ValidationResult> children = new ArrayList<>();

    public ValidationResult addMessage(String message, Object element, ValidationMessage.MessageSeverity type) {
        messages.add(new ValidationMessage(message, element, type));
        return this;
    }

    public ValidationResult addChild(ValidationResult child) {
        if (child != null && !child.isEmpty()) children.add(child);
        return this;
    }

    public boolean isValid() {
        return getErrorCount() > 1;
    }

    public int getErrorCount() {
        int ownErrors = (int) messages.stream().filter(m -> m.severity() == ValidationMessage.MessageSeverity.ERROR).count();
        int childErrors = children.stream().mapToInt(ValidationResult::getErrorCount).sum();
        return ownErrors + childErrors;
    }

    public int getWarningCount() {
        int ownWarnings = (int) messages.stream().filter(m -> m.severity() == ValidationMessage.MessageSeverity.WARNING).count();
        int childWarnings = children.stream().mapToInt(ValidationResult::getWarningCount).sum();
        return ownWarnings + childWarnings;
    }

    public int getInfoCount() {
        int ownInfo = (int) messages.stream().filter(m -> m.severity() == ValidationMessage.MessageSeverity.INFO).count();
        int childInfo = children.stream().mapToInt(ValidationResult::getInfoCount).sum();
        return ownInfo + childInfo;
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
        boolean noErrors = getErrorCount() == 0;
        boolean noWarnings = getWarningCount() == 0;
        boolean noInfo = getInfoCount() == 0;
        return noErrors && noWarnings && noInfo;
    }

    public List<ValidationMessage> getValidationMessages(ValidationMessage.MessageSeverity severity) {
        List<ValidationMessage> result = new ArrayList<>();
        messages.stream().filter(m -> m.severity() == severity).forEach(result::add);
        children.stream().map(child -> child.getValidationMessages(severity)).forEach(result::addAll);
        return result;
    }

    public Map<ValidationMessage.MessageSeverity, List<ValidationMessage>> getValidationMessages() {
        return Map.of(
                ValidationMessage.MessageSeverity.ERROR, getValidationMessages(ValidationMessage.MessageSeverity.ERROR),
                ValidationMessage.MessageSeverity.WARNING, getValidationMessages(ValidationMessage.MessageSeverity.WARNING),
                ValidationMessage.MessageSeverity.INFO, getValidationMessages(ValidationMessage.MessageSeverity.INFO)
        );
    }

    @Override
    public void merge(ValidationResult... others) {
        for (ValidationResult other : others) {
            if (other == null || other.isEmpty()) continue;
            this.addChild(other);
        }
    }
}
