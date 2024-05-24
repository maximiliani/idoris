package edu.kit.datamanager.idoris.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Node("OperationTypeProfile")
@Getter
@Setter
public class OperationTypeProfile extends GenericIDORISEntity {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private List<TypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.OUTGOING)
    private List<ProfileAttribute> attributes;

    private String name;
    private String description;

    public OperationTypeProfile(String pid, Long version, Instant createdAt, Instant lastModifiedAt, Set<User> contributors, License license, List<TypeProfile> inheritsFrom, List<ProfileAttribute> attributes, String name, String description) {
        super(pid, version, createdAt, lastModifiedAt, contributors, license);
        this.inheritsFrom = inheritsFrom;
        this.attributes = attributes;
        this.name = name;
        this.description = description;
    }
}
