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

import com.google.auto.service.AutoService;
import edu.kit.datamanager.idoris.domain.VisitableElement;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.lang.reflect.Modifier;
import java.util.*;

@SupportedAnnotationTypes("edu.kit.datamanager.idoris.rules.logic.Rule")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public final class RuleProcessor extends AbstractProcessor {

    /**
     * Keeps a per-(task,visitableType) grouping while we scan the source set.
     */
    private final Map<RuleTask,
            Map<String /*visitable FQN*/, Map<String /*processor FQN*/, RuleInfo>>> collected
            = new HashMap<>();

    private static List<RuleInfo> topologicallySort(Map<String, RuleInfo> ruleMap) {
        Map<String, List<String>> edges = new HashMap<>();
        ruleMap.values().forEach(info -> edges.put(info.qualifiedName(), new ArrayList<>()));

        for (RuleInfo info : ruleMap.values()) {
            String from = info.qualifiedName();
            for (Class<? extends IRuleProcessor> dep : info.dependsOn()) {
                String depName = dep.getCanonicalName();
                if (edges.containsKey(depName)) edges.get(from).add(depName);
            }
            for (Class<? extends IRuleProcessor> after : info.executeBefore()) {
                String afterName = after.getCanonicalName();
                if (edges.containsKey(afterName)) edges.get(afterName).add(from);
            }
        }

        Map<String, State> state = new HashMap<>();
        List<RuleInfo> ordered = new ArrayList<>();
        for (String n : edges.keySet()) {
            if (state.get(n) == null) dfs(n, edges, state, ordered, ruleMap);
        }
        return ordered;
    }

    private static void dfs(String current,
                            Map<String, List<String>> edges,
                            Map<String, State> state,
                            List<RuleInfo> ordered,
                            Map<String, RuleInfo> ruleMap) {
        state.put(current, State.VISITING);
        for (String nxt : edges.getOrDefault(current, List.of())) {
            if (state.get(nxt) == State.VISITING) {
                throw new IllegalStateException(current + " <-> " + nxt);
            }
            if (state.get(nxt) == null) dfs(nxt, edges, state, ordered, ruleMap);
        }
        state.put(current, State.VISITED);
        ordered.add(ruleMap.get(current));
    }

    /* ------------------------------------------------------------------ */
    /*  DAG building & cycle detection (compile time!)                    */
    /* ------------------------------------------------------------------ */

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {                 // Step 2 – graph building & validation
            validateDag();
            // optionally generate metadata file(s) for runtime
            return false;
        }

        // Step 1 – collect all @Rule elements that appear in this compilation round
        for (Element element : roundEnv.getElementsAnnotatedWith(Rule.class)) {
            if (!(element instanceof TypeElement typeElement)) {
                continue;                                // Defensive (never happens in practice)
            }
            Rule rule = element.getAnnotation(Rule.class);
            scanRule(typeElement, rule);
        }
        return false;                                    // allow other processors to see @Rule
    }

    /* ––––– copy/paste of topologicallySort(..) & dfs(..) from runtime version ––– */

    private void scanRule(TypeElement element, Rule rule) {
        RuleTask[] tasks = rule.tasks().length == 0 ? RuleTask.values() : rule.tasks();

        for (Class<? extends VisitableElement> visitable : rule.appliesTo()) {
            if (Modifier.isAbstract(visitable.getModifiers())) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "[idoris-rules] Rule " + rule.name() + " cannot be applied to abstract type "
                                + visitable.getCanonicalName() + ".",
                        element);
                continue;                               // skip abstract types
            }

            String visitableFqn = visitable.getCanonicalName();
            for (RuleTask task : tasks) {
                collected
                        .computeIfAbsent(task, t -> new HashMap<>())
                        .computeIfAbsent(visitableFqn, v -> new HashMap<>())
                        .put(element.getQualifiedName().toString(),
                                new RuleInfo(element, rule));
            }
        }
    }

    private void validateDag() {
        collected.forEach((task, byType) -> byType.forEach((visitable, ruleMap) -> {
            try {
                topologicallySort(ruleMap);              // re-use DFS from runtime version
            } catch (IllegalStateException ex) {
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "[idoris-rules] Cycle detected for task "
                                + task + " / type " + visitable + ": " + ex.getMessage());
            }
        }));
    }

    private enum State {VISITING, VISITED}

    /* ------------------------------------------------------------------ */
    private record RuleInfo(TypeElement element, Rule rule) {
        String qualifiedName() {
            return element.getQualifiedName().toString();
        }

        Class<? extends IRuleProcessor>[] dependsOn() {
            return rule.dependsOn();
        }

        Class<? extends IRuleProcessor>[] executeBefore() {
            return rule.executeBefore();
        }
    }
}