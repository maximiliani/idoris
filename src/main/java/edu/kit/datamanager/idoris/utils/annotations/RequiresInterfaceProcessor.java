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

package edu.kit.datamanager.idoris.utils.annotations;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("RequiresInterface")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RequiresInterfaceProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return false; // No annotations to process
        }
        // Iterate over all elements annotated with @RequiresInterface
        for (Element element : roundEnv.getElementsAnnotatedWith(RequiresInterface.class)) {
            // Ensure the element is a class
            if (element.getKind() != ElementKind.CLASS) continue;

            // Check if the class implements the required interface
            TypeElement classElement = (TypeElement) element;
            RequiresInterface annotation = classElement.getAnnotation(RequiresInterface.class);
            String requiredInterface = annotation.value().getCanonicalName();

            boolean implementsInterface = classElement.getInterfaces().stream()
                    .map(TypeMirror::toString)
                    .anyMatch(iface -> iface.equals(requiredInterface));

            if (!implementsInterface) {
                // If the class does not implement the required interface, report an error
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Class " + classElement.getSimpleName() +
                                " must implement interface " + requiredInterface,
                        classElement
                );
            }
        }
        // Indicate that the annotations have been processed
        return false;
    }
}

