package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("OperationTypeProfile")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class OperationTypeProfile extends GenericIDORISEntity {

    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<OperationTypeProfile> inheritsFrom;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private Set<Attribute> attributes;

    @Relationship(value = "outputs", direction = Relationship.Direction.OUTGOING)
    private Set<Attribute> outputs;

    @Relationship(value = "adapters", direction = Relationship.Direction.OUTGOING)
    private Set<FDO> adapters;

    private String name;
    private String description;
}
