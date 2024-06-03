package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.enums.Obligation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class Attribute {
    @Id
    @GeneratedValue
    private String id;

    @Relationship(value = "dataType", direction = Relationship.Direction.OUTGOING)
    private DataType dataType;

    private String name;
    private String description;
    private Obligation obligation;
    private boolean repeatable;
}
