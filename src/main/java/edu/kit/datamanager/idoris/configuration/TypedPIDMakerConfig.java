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

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties("idoris.typed-pid-maker")
@Validated
@AutoConfigureAfter(value = ApplicationProperties.class)
@ConditionalOnBean(value = ApplicationProperties.class)
@ConditionalOnExpression(
        "#{ '${idoris.pid-generation}' eq T(edu.kit.datamanager.idoris.configuration.ApplicationProperties.PIDGeneration).TYPED_PID_MAKER.name() }"
)
@Getter
@Setter
public class TypedPIDMakerConfig {
    /**
     * Put metadata of the AdministrativeMetadata into the PID record.
     *
     * @see edu.kit.datamanager.idoris.domain.GenericIDORISEntity
     */
    private boolean meaningfulPIDRecords = true;

    /**
     * Update existing PID records with the latest metadata from the AdministrativeMetadata.
     * If set to false, existing PID records will not be updated,
     * but new records will still be created with the latest metadata.
     */
    private boolean updatePIDRecords = true;

    /**
     * The base URL for the Typed PID Maker service.
     * This is required when the service is enabled.
     */
    @NotNull(message = "Base URL is required when enabled is true")
    private String baseUrl;

    /**
     * The timeout in milliseconds for requests to the Typed PID Maker service.
     */
    private int timeout = 5000;
}
