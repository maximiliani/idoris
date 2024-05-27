package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.enums.SubSchemaRelation;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Node("TypeProfile")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class TypeProfile extends GenericIDORISEntity {

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
}
