package edu.kit.datamanager.idoris.domain;

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
public class FDOValue {
    @RelationshipId
    private Long id;

    private String value;

    @TargetNode
    private IValueSpecification valueSpecification;

    public FDOValue(String value, IValueSpecification valueSpecification) {
        this.value = value;
        this.valueSpecification = valueSpecification;
    }
}
