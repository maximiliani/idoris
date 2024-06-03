package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.enums.SubSchemaRelation;
import edu.kit.datamanager.idoris.domain.relationships.ProfileAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("TypeProfile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TypeProfile extends DataType {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<TypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.OUTGOING)
    private Set<ProfileAttribute> attributes;

    private String restrictions;
    private SubSchemaRelation subSchemaRelation = SubSchemaRelation.allowAdditionalProperties;
}
