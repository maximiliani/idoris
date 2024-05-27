package edu.kit.datamanager.idoris.domain.relationships;

import edu.kit.datamanager.idoris.domain.entities.Standard;
import edu.kit.datamanager.idoris.domain.enums.NatureOfApplicability;
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
public class StandardApplicability {
    @RelationshipId
    private String id;

    private NatureOfApplicability natureOfApplicability;
    private String details;

    @TargetNode
    private Standard standard;
}
