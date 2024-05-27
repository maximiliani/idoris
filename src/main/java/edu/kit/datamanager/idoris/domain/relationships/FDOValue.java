package edu.kit.datamanager.idoris.domain.relationships;

import edu.kit.datamanager.idoris.domain.entities.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.io.Serializable;

@RelationshipProperties
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class FDOValue implements Serializable {
    @RelationshipId
    private String pid;

    private String value;

    @TargetNode
    private DataType dataType;
}
