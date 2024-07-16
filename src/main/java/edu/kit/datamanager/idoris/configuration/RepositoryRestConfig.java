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

package edu.kit.datamanager.idoris.configuration;

import edu.kit.datamanager.idoris.dao.IOperationDao;
import edu.kit.datamanager.idoris.dao.ITypeProfileDao;
import edu.kit.datamanager.idoris.domain.VisitableElement;
import edu.kit.datamanager.idoris.domain.entities.*;
import edu.kit.datamanager.idoris.validators.VisitableElementValidator;
import edu.kit.datamanager.idoris.visitors.*;
import io.netty.util.Attribute;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class RepositoryRestConfig implements RepositoryRestConfigurer {
    private final ApplicationProperties applicationProperties;

    @Autowired
    public RepositoryRestConfig(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.exposeIdsFor(
                Attribute.class,
                BasicDataType.class,
                FDO.class,
                Operation.class,
                OperationStep.class,
                OperationTypeProfile.class,
                TypeProfile.class
        );

        cors.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeSave", new VisitableElementValidator(applicationProperties));
        v.addValidator("beforeCreate", new VisitableElementValidator(applicationProperties));
        v.addValidator("beforeLinkSave", new VisitableElementValidator(applicationProperties));
    }

    @Bean
    public RepresentationModelProcessor<EntityModel<TypeProfile>> typeProfileProcessor() {
        LinkBuilder baseLink = linkTo(ITypeProfileDao.class).slash("api").slash("typeProfiles");
        return new RepresentationModelProcessor<EntityModel<TypeProfile>>() {
            @Override
            public EntityModel<TypeProfile> process(@NotNull EntityModel<TypeProfile> model) {
                String pid = Objects.requireNonNull(model.getContent()).getPid();
                model.add(baseLink.slash(pid).slash("validate").withRel("validate"));
//                model.add(baseLink.slash(pid).slash("attributes").withRel("attributes"));
                model.add(baseLink.slash(pid).slash("inheritedAttributes").withRel("inheritedAttributes"));
                model.add(baseLink.slash(pid).slash("inheritanceTree").withRel("inheritanceTree"));
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
    public RepresentationModelProcessor<RepresentationModel<?>> validatorProcessor() {
        return model -> {
            if (model instanceof EntityModel<?> && ((EntityModel<?>) model).getContent() instanceof VisitableElement element) {
                Set<Visitor<ValidationResult>> validators = Set.of(new SubSchemaRelationValidator(), new SyntaxValidator());

                AtomicInteger errorCount = new AtomicInteger();
                AtomicInteger warningCount = new AtomicInteger();

                Map<String, ValidationResult> results = validators.stream()
                        .map(visitor -> Map.entry(visitor.getClass().getSimpleName(), element.execute(visitor)))
                        .filter(entry -> {
                            ApplicationProperties.ValidationLevel validationLevel = applicationProperties.getValidationLevel();
                            if (validationLevel == ApplicationProperties.ValidationLevel.ERROR) {
                                return entry.getValue().getErrorCount() > 0;
                            } else if (validationLevel == ApplicationProperties.ValidationLevel.WARNING) {
                                return entry.getValue().getErrorCount() > 0 || entry.getValue().getWarningCount() > 0;
                            } else if (validationLevel == ApplicationProperties.ValidationLevel.INFO) {
                                return !entry.getValue().isEmpty();
                            }
                            return false;
                        })
                        .peek(entry -> {
                            errorCount.addAndGet(entry.getValue().getErrorCount());
                            warningCount.addAndGet(entry.getValue().getWarningCount());
                        })
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

//                if (errorCount.get() > 0) {
//                    throw new ValidationException(results);
//                }

                if (errorCount.get() > 0 || warningCount.get() > 0) {
                    return CollectionModel.of(Set.of(results, model));
                }

            }
            return model;
        };
    }

    @ControllerAdvice
    @Log
    public static class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
        //        @ExceptionHandler(MethodArgumentNotValidException.class)
        @Override
        protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
            String errors = ex.getAllErrors()
                    .stream()
                    .map(ObjectError::toString)
                    .collect(Collectors.joining("\n"));

            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        @Override
        protected ResponseEntity<Object> handleMethodValidationException(MethodValidationException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
            String errors = ex.getAllValidationResults()
                    .stream()
                    .map(ParameterValidationResult::toString)
                    .collect(Collectors.joining("\n"));

            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler
        public ResponseEntity<?> handle(ValidationException exception, WebRequest request) {
//            if (!request.getMethod().equalsIgnoreCase("GET"))
//            return handleExceptionInternal(exception, exception.getValidationResult(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
            return new ResponseEntity<>(exception.getValidationResults(), HttpStatus.BAD_REQUEST);
        }
    }
}
