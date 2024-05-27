package edu.kit.datamanager.idoris.domain.relationships;

import edu.kit.datamanager.idoris.domain.entities.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

/**
 * Used by FDOperations
 */
@RelationshipProperties
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class ValueSpecificationRelation {
    @RelationshipId
    private String id;

    @TargetNode
    private DataType dataType;

    private String description;
    private String internal_reference;
}
