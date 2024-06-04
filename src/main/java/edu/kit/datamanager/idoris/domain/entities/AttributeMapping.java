package edu.kit.datamanager.idoris.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;

@Node("AttributeMapping")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AttributeMapping implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    @Relationship(value = "input", direction = Relationship.Direction.INCOMING)
    private Attribute input;

    private String name;
    private String value;
    private int index;

    @Relationship(value = "output", direction = Relationship.Direction.OUTGOING)
    private Attribute output;
}
