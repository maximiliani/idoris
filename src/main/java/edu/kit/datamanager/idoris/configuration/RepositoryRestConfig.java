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

package edu.kit.datamanager.idoris.configuration;

import edu.kit.datamanager.idoris.dao.IOperationDao;
import edu.kit.datamanager.idoris.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.domain.entities.*;
import edu.kit.datamanager.idoris.rules.logic.OutputMessage;
import edu.kit.datamanager.idoris.rules.logic.RuleService;
import edu.kit.datamanager.idoris.rules.logic.RuleTask;
import edu.kit.datamanager.idoris.rules.validation.ValidationResult;
import io.netty.util.Attribute;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
@Log
public class RepositoryRestConfig implements RepositoryRestConfigurer {
    private final ApplicationProperties applicationProperties;
    private final RuleService ruleService;

    @Autowired
    public RepositoryRestConfig(ApplicationProperties applicationProperties, RuleService ruleService) {
        this.applicationProperties = applicationProperties;
        this.ruleService = ruleService;
    }


    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(
                Attribute.class,
                AtomicDataType.class,
                Operation.class,
                TechnologyInterface.class,
                TypeProfile.class
        );

        cors.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        RuleBasedVisitableElementValidator validator = new RuleBasedVisitableElementValidator(ruleService, applicationProperties);
        v.addValidator("beforeSave", validator);
        v.addValidator("beforeCreate", validator);
        v.addValidator("afterLinkSave", validator);
    }

    @Bean
    public RepresentationModelProcessor<EntityModel<TypeProfile>> typeProfileProcessor() {
        LinkBuilder baseLink = linkTo(ITypeProfileDao.class).slash("api").slash("typeProfiles");
        return new RepresentationModelProcessor<EntityModel<TypeProfile>>() {
            @Override
            public EntityModel<TypeProfile> process(EntityModel<TypeProfile> model) {
                String pid = Objects.requireNonNull(model.getContent()).getPid();
                model.add(baseLink.slash(pid).slash("validate").withRel("validate"));
                model.add(baseLink.slash(pid).slash("inheritedAttributes").withRel("inheritedAttributes"));
                model.add(baseLink.slash(pid).slash("inheritanceTree").withRel("inheritanceTree"));
                return model;
            }
        };
    }

    @Bean
    public RepresentationModelProcessor<EntityModel<DataType>> dataTypeProcessor() {
        return new RepresentationModelProcessor<EntityModel<DataType>>() {
            @Override
            public EntityModel<DataType> process(EntityModel<DataType> model) {
                String pid = Objects.requireNonNull(model.getContent()).getPid();
                model.add(Link.of(linkTo(IOperationDao.class)
                        .slash("api")
                        .slash("operations")
                        .slash("search")
                        .slash("getOperationsForDataType")
                        .toUri() + "?pid=" + pid, "operations"));
                return model;
            }
        };
    }

    @Bean
    public RepresentationModelProcessor<RepresentationModel<?>> validatorProcessor(RuleService ruleService) {
        return model -> {
            if (model instanceof EntityModel<?> && ((EntityModel<?>) model).getContent() instanceof VisitableElement element) {
                // Use RuleService to process validation with the VALIDATE task
                ValidationResult validationResult = ruleService.process(
                        RuleTask.VALIDATE,
                        element,
                        ValidationResult::new
                );

                // Convert ValidationResult to the expected format for the response
                if (!validationResult.isEmpty()) {
                    Map<OutputMessage.MessageSeverity, List<OutputMessage>> filteredMessages =
                            validationResult.getOutputMessages()
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
                                    .filter(entry -> !entry.getValue().isEmpty())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    if (!filteredMessages.isEmpty()) {
                        Map<String, Object> results = Map.of(
                                "validationResult", filteredMessages,
                                "originalModel", model
                        );
                        return CollectionModel.of(Set.of(results));
                    }
                }
            }
            return model;
        };
    }

    /**
     * Spring Data REST validator that uses the new rule-based validation system.
     * This validator integrates with Spring's validation framework and executes
     * all applicable validation rules through the RuleService.
     */
    private static class RuleBasedVisitableElementValidator implements org.springframework.validation.Validator {
        private final RuleService ruleService;
        private final ApplicationProperties applicationProperties;

        public RuleBasedVisitableElementValidator(RuleService ruleService, ApplicationProperties applicationProperties) {
            this.ruleService = ruleService;
            this.applicationProperties = applicationProperties;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return VisitableElement.class.isAssignableFrom(clazz);
        }

        @Override
        public void validate(Object target, Errors errors) {
            if (!(target instanceof VisitableElement element)) {
                return;
            }

            try {
                // Execute validation rules using RuleService
                ValidationResult result = ruleService.process(
                        RuleTask.VALIDATE,
                        element,
                        ValidationResult::new
                );

                // Convert ValidationResult to Spring validation errors
                convertToSpringErrors(result, errors);

            } catch (Exception e) {
                log.severe("Error during rule-based validation: " + e.getMessage());
                errors.reject("validation.error", "Validation failed due to internal error");
            }
        }

        /**
         * Converts ValidationResult messages to Spring validation errors.
         * Only includes messages that meet the configured validation level threshold.
         */
        private void convertToSpringErrors(ValidationResult result, org.springframework.validation.Errors errors) {
            if (result == null || result.isEmpty()) {
                return;
            }

            result.getOutputMessages().entrySet().stream()
                    .filter(entry -> entry.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
                    .forEach(entry -> {
                        OutputMessage.MessageSeverity severity = entry.getKey();
                        List<OutputMessage> messages = entry.getValue();

                        for (OutputMessage message : messages) {
                            String errorCode = "validation." + severity.name().toLowerCase();
                            String defaultMessage = message.message();

                            if (severity == OutputMessage.MessageSeverity.ERROR) {
                                errors.reject(errorCode, defaultMessage);
                            } else {
                                // For warnings and info, we can still add them but they won't fail validation
                                errors.reject("validation.warning", defaultMessage);
                            }
                        }
                    });
        }
    }
}