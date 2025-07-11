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

package edu.kit.datamanager.idoris.pids.client;

import edu.kit.datamanager.idoris.configuration.TypedPIDMakerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration class for the TypedPIDMakerClient.
 * This class registers the TypedPIDMakerClient as a bean in the Spring application context.
 */
@Configuration
@ConditionalOnBean(TypedPIDMakerConfig.class)
public class TypedPIDMakerClientConfig {

    /**
     * Creates a TypedPIDMakerClient bean using Spring HTTP Interface.
     *
     * @param config The TypedPIDMakerConfig
     * @return The TypedPIDMakerClient
     */
    @Bean
    public TypedPIDMakerClient typedPIDMakerClient(TypedPIDMakerConfig config) {
        // Create a client HTTP request factory with the configured timeout
        org.springframework.http.client.ClientHttpRequestFactory requestFactory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();

        RestClient restClient = RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.setAccept(java.util.List.of(org.springframework.http.MediaType.parseMediaType("application/vnd.datamanager.pid.simple+json"))))
                .defaultStatusHandler(org.springframework.http.HttpStatusCode::isError, (request, response) -> {
                    throw new org.springframework.web.client.RestClientException("Error response: " + response.getStatusCode());
                })
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();

        return factory.createClient(TypedPIDMakerClient.class);
    }
}