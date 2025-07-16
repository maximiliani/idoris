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

package edu.kit.datamanager.idoris.configuration;

import edu.kit.datamanager.idoris.datatypes.entities.AtomicDataType;
import edu.kit.datamanager.idoris.datatypes.entities.DataType;
import edu.kit.datamanager.idoris.datatypes.entities.TypeProfile;
import edu.kit.datamanager.idoris.datatypes.web.v1.AtomicDataTypeController;
import edu.kit.datamanager.idoris.datatypes.web.v1.TypeProfileController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;

import java.util.Objects;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Configuration class for HATEOAS-related settings.
 * This class configures representation model processors and other HATEOAS-related beans.
 */
@Configuration
public class HateoasConfig {

    /**
     * Creates a representation model processor for TypeProfile entities.
     * This processor adds links to validate, get inherited attributes, and get the inheritance tree.
     *
     * @return a representation model processor for TypeProfile entities
     */
    @Bean
    public RepresentationModelProcessor<EntityModel<TypeProfile>> typeProfileProcessor() {
        return new RepresentationModelProcessor<EntityModel<TypeProfile>>() {
            @Override
            public EntityModel<TypeProfile> process(EntityModel<TypeProfile> model) {
                TypeProfile typeProfile = Objects.requireNonNull(model.getContent());
                String pid = typeProfile.getPid();

                // Add links to related resources
                model.add(linkTo(methodOn(TypeProfileController.class).validate(pid)).withRel("validate"));
                model.add(linkTo(methodOn(TypeProfileController.class).getInheritedAttributes(pid)).withRel("inheritedAttributes"));
                model.add(linkTo(methodOn(TypeProfileController.class).getInheritanceTree(pid)).withRel("inheritanceTree"));

                return model;
            }
        };
    }

    /**
     * Creates a representation model processor for DataType entities.
     * This processor adds links to operations for the data type.
     *
     * @return a representation model processor for DataType entities
     */
    @Bean
    public RepresentationModelProcessor<EntityModel<DataType>> dataTypeProcessor() {
        return new RepresentationModelProcessor<EntityModel<DataType>>() {
            @Override
            public EntityModel<DataType> process(EntityModel<DataType> model) {
                DataType dataType = Objects.requireNonNull(model.getContent());
                String pid = dataType.getPid();

                // Add link to operations for this data type
                // The link depends on the type of DataType
                if (dataType instanceof AtomicDataType) {
                    model.add(linkTo(methodOn(AtomicDataTypeController.class).getOperationsForAtomicDataType(pid)).withRel("operations"));
                } else if (dataType instanceof TypeProfile) {
                    model.add(linkTo(methodOn(TypeProfileController.class).getOperationsForTypeProfile(pid)).withRel("operations"));
                }

                return model;
            }
        };
    }

//    /**
//     * Creates a representation model processor that adds validation results to entity models.
//     * This processor executes validation rules and adds the results to the model.
//     *
//     * @param ruleService the rule service
//     * @param applicationProperties the application properties
//     * @return a representation model processor that adds validation results
//     */
//    @Bean
//    public RepresentationModelProcessor<RepresentationModel<?>> validatorProcessor(
//            RuleService ruleService,
//            ApplicationProperties applicationProperties) {
//
//        return model -> {
//            if (model instanceof EntityModel<?> && ((EntityModel<?>) model).getContent() instanceof VisitableElement element) {
//                // Use RuleService to process validation with the VALIDATE task
//                ValidationResult validationResult = ruleService.executeRules(
//                        RuleTask.VALIDATE,
//                        element,
//                        ValidationResult::new
//                );
//
//                // Convert ValidationResult to the expected format for the response
//                if (!validationResult.isEmpty()) {
//                    Map<OutputMessage.MessageSeverity, List<OutputMessage>> filteredMessages =
//                            validationResult.getOutputMessages()
//                                    .entrySet()
//                                    .stream()
//                                    .filter(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
//                                    .filter(entry -> !entry.getValue().isEmpty())
//                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//                    if (!filteredMessages.isEmpty()) {
//                        Map<String, Object> results = Map.of(
//                                "validationResult", filteredMessages,
//                                "originalModel", model
//                        );
//                        return CollectionModel.of(Set.of(results));
//                    }
//                }
//            }
//            return model;
//        };
//    }
}
