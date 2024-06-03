package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.relationships.AttributeReference;
import edu.kit.datamanager.idoris.domain.relationships.ValueSpecificationRelation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;
import java.util.Set;

@Node("Operation")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class Operation extends GenericIDORISEntity {

    @Relationship(value = "executableOn", direction = Relationship.Direction.OUTGOING)
    private AttributeReference executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.INCOMING)
    private Set<ValueSpecificationRelation> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.OUTGOING)
    private Set<AttributeReference> environment;

    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> execution;

    private String name;
    private String description;

}
