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

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * This class is used to configure the application.
 * It reads the values from the application.properties file.
 *
 * @author maximiliani
 */
@ConfigurationProperties(prefix = "idoris")
@Component
@Data
@Getter
@Validated
@EqualsAndHashCode
public class ApplicationProperties {

    @Value("${idoris.validation-policy}")
    @NotNull
    private ValidationPolicy validationPolicy = ValidationPolicy.LAX;

    @Value("${idoris.validation-level}")
    @NotNull
    private ValidationLevel validationLevel = ValidationLevel.INFO;

    /**
     * The policy to use for validating the input.
     * <p>
     * The policy can be either STRICT or LENIENT.
     * Strict means that the input must be exactly as expected. Warnings are treated as errors and therefore fail the validation.
     * Lenient means that the input can be slightly different from the expected input. Warnings are provided to the user but DO NOT fail the validation.
     */
    public enum ValidationPolicy {
        STRICT, LAX
    }

    /**
     * The lowest level of validation that should be outputted to the user.
     * <p>
     * The level can be either ERROR, WARNING or INFO.
     * Error means that only errors are outputted to the user. e.g. if critical information is missing.
     * Warning means that errors and warnings are outputted to the user. e.g. if there are minor issues with the input.
     * Info means that errors, warnings and info messages are outputted to the user. e.g. if the input is correct but there are some additional information or small feedback that could be useful.
     */
    public enum ValidationLevel {
        ERROR, WARNING, INFO
    }
}
