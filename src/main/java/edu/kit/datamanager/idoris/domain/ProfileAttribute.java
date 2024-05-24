package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.io.Serializable;

@RelationshipProperties
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ProfileAttribute implements Serializable {
    @RelationshipId
    private String id;

    private String name;
    private String description;
    private boolean repeatable = false;
    private Obligation obligation = Obligation.Optional;
    private String defaultValue;
    private String constantValue;

    @TargetNode
    private IValueSpecification pid;

    @Getter
    @AllArgsConstructor
    public enum Obligation {
        Mandatory("Mandatory"),
        Optional("Optional");
        private final String name;
    }
}
