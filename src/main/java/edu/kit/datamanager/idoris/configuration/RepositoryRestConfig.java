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
import edu.kit.datamanager.idoris.validators.ValidationMessage;
import edu.kit.datamanager.idoris.validators.ValidationResult;
import edu.kit.datamanager.idoris.validators.VisitableElementValidator;
import edu.kit.datamanager.idoris.visitors.InheritanceValidator;
import edu.kit.datamanager.idoris.visitors.SubSchemaRelationValidator;
import edu.kit.datamanager.idoris.visitors.SyntaxValidator;
import edu.kit.datamanager.idoris.visitors.Visitor;
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
        v.addValidator("afterLinkSave", new VisitableElementValidator(applicationProperties));
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
    public RepresentationModelProcessor<RepresentationModel<?>> validatorProcessor() {
        return model -> {
            if (model instanceof EntityModel<?> && ((EntityModel<?>) model).getContent() instanceof VisitableElement element) {
                Set<Visitor<ValidationResult>> validators = Set.of(
                        new SubSchemaRelationValidator(),
                        new SyntaxValidator(),
                        new InheritanceValidator()
                );

                Map<String, Map<ValidationMessage.MessageSeverity, List<ValidationMessage>>> results = validators.stream()
                        .map(visitor -> Map.entry(visitor.getClass().getSimpleName(), element.execute(visitor)))
                        .map(entry -> Map.entry(
                                entry.getKey(),
                                entry.getValue()
                                        .getValidationMessages()
                                        .entrySet()
                                        .stream()
                                        .filter(e -> e.getKey().isHigherOrEqualTo(applicationProperties.getValidationLevel()))
                                        .filter(e -> !e.getValue().isEmpty())
                                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))))
                        .filter(entry -> !entry.getValue().isEmpty())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                if (!results.isEmpty()) return CollectionModel.of(Set.of(results, model));
            }
            return model;
        };
    }
}
