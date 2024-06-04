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

package edu.kit.datamanager.idoris.domain.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node("User")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ORCiDUser.class, name = "orcid"),
        @JsonSubTypes.Type(value = TextUser.class, name = "text")
})
public abstract class User implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String type;

//    private Type type;
//    private String name;
//    private String email;
//    private String details;
//    private String url;

//    public User(Type type, Optional<String> name, Optional<String> email, Optional<String> details, Optional<String> url) {
//        this.type = type;
//        this.name = name.orElse(null);
//        this.email = email.orElse(null);
//        this.details = details.orElse(null);
//        this.url = url.orElse(null);
//    }

//    @AllArgsConstructor
//    @Getter
//    public enum Type {
//        Handle("Handle"),
//        ORCiD("ORCiD"),
//        URL("URL"),
//        Text("Text");
//
//        private final String name;
//    }
}

