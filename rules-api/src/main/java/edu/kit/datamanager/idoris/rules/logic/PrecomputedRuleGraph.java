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

import java.util.List;
import java.util.Map;

/**
 * Represents a precomputed rule dependency graph generated at compile time.
 *
 * <p>This class stores the results of the topological sort performed by the
 * RuleAnnotationProcessor for use at runtime, eliminating the need
 * for duplicate validation and sorting in the RuleService.
 */
public class PrecomputedRuleGraph {

    /**
     * Map of rule tasks to their target types and sorted rule classes.
     * Structure: RuleTask → TargetType → Topologically sorted rule class names
     */
    private final Map<RuleTask, Map<String, List<String>>> sortedRulesByTaskAndType;

    /**
     * Constructs a new precomputed rule graph with the given sorted rules.
     *
     * @param sortedRulesByTaskAndType map of rule tasks to their target types and sorted rule classes
     */
    public PrecomputedRuleGraph(Map<RuleTask, Map<String, List<String>>> sortedRulesByTaskAndType) {
        this.sortedRulesByTaskAndType = sortedRulesByTaskAndType;
    }

    /**
     * Gets the sorted rule class names for the given task and target type.
     *
     * @param task       the rule task
     * @param targetType the fully qualified name of the target type
     * @return list of fully qualified rule class names in topological order
     */
    public List<String> getSortedRuleClasses(RuleTask task, String targetType) {
        return sortedRulesByTaskAndType
                .getOrDefault(task, Map.of())
                .getOrDefault(targetType, List.of());
    }

    /**
     * Gets the complete map of sorted rules by task and target type.
     *
     * @return map of rule tasks to their target types and sorted rule classes
     */
    public Map<RuleTask, Map<String, List<String>>> getSortedRulesByTaskAndType() {
        return sortedRulesByTaskAndType;
    }
}
