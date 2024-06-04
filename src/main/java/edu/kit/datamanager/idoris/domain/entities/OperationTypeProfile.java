package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.relationships.AttributeReference;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("OperationTypeProfile")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OperationTypeProfile extends GenericIDORISEntity {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<OperationTypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private Set<AttributeReference> attributes;

    @Relationship(value = "outputs", direction = Relationship.Direction.OUTGOING)
    private Set<AttributeReference> outputs;

    @Relationship(value = "adapters", direction = Relationship.Direction.OUTGOING)
    private Set<FDO> adapters;

    private String name;
    private String description;
}
