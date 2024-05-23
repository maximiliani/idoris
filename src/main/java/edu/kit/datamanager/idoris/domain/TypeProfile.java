package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Node("TypeProfile")
@Getter
@Setter
public class TypeProfile extends GenericIDORISEntity implements IValueSpecification {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private List<TypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.OUTGOING)
    private List<ProfileAttribute> attributes;

    private String name;
    private String description;
    private String restrictions;
    private SubSchemaRelation subSchemaRelation = SubSchemaRelation.allowAdditionalProperties;
    @Property("default")
    private String defaultValue;

    public TypeProfile(PID pid, Long version, Instant createdAt, Instant lastModifiedAt, Set<User> contributors, License license, List<TypeProfile> inheritsFrom, List<ProfileAttribute> attributes, String name, String description, String restrictions, SubSchemaRelation subSchemaRelation, String defaultValue) {
        super(pid, version, createdAt, lastModifiedAt, contributors, license);
        this.inheritsFrom = inheritsFrom;
        this.attributes = attributes;
        this.name = name;
        this.description = description;
        this.restrictions = restrictions;
        this.subSchemaRelation = subSchemaRelation;
        this.defaultValue = defaultValue;
    }

    @AllArgsConstructor
    @Getter
    public enum SubSchemaRelation {
        denyAdditionalProperties("denyAdditionalProperties"),
        allowAdditionalProperties("allowAdditionalProperties"),
        requireAllProperties("requireAllProperties"),
        requireAnyOfProperties("requireAnyOfProperties"),
        requireOneOfProperties("requireOneOfProperties"),
        requireNoneOfProperties("requireNoneOfProperties");
        private final String name;
    }
}
