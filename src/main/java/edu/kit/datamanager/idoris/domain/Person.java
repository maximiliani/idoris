package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Optional;

@Node("Person")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Person {
    @Id
    @GeneratedValue
    private long id;

    private Type type;
    private String name;
    private String email;
    private String details;
    private String url;

    public Person(Type type, Optional<String> name, Optional<String> email, Optional<String> details, Optional<String> url) {
        this.type = type;
        this.name = name.orElse(null);
        this.email = email.orElse(null);
        this.details = details.orElse(null);
        this.url = url.orElse(null);
    }

    @AllArgsConstructor
    @Getter
    public enum Type {
        Handle("Handle"),
        ORCiD("ORCiD"),
        URL("URL"),
        Text("Text");

        private final String name;
    }
}
