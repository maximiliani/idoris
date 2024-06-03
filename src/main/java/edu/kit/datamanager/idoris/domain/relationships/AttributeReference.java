package edu.kit.datamanager.idoris.domain.relationships;

import edu.kit.datamanager.idoris.domain.entities.Attribute;
import edu.kit.datamanager.idoris.domain.enums.Obligation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class AttributeReference {
    @RelationshipId
    private String id;

    @TargetNode
    private Attribute attribute;
    private String value;
    private String internalReference;
    private Obligation obligation;
    private boolean repeatable;
}
