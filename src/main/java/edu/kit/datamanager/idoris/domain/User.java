package edu.kit.datamanager.idoris.domain;

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
@RequiredArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private Type type;
    private String name;
    private String email;
    private String details;
    private String url;

    public User(Type type, String name, String email, String details, String url) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.details = details;
        this.url = url;
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
