package edu.kit.datamanager.idoris.domain;

import lombok.*;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("FDO")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
public class FDO {
    @Id
    private String pid;

    @Relationship(value = "hasValues", direction = Relationship.Direction.OUTGOING)
    private Set<FDOValue> recordValues;

    @Relationship(value = "confirmsWith", direction = Relationship.Direction.OUTGOING)
    private Set<TypeProfile> typeProfiles;

    public void addTypeProfile(TypeProfile typeProfile) {
        typeProfiles.add(typeProfile);
    }

    public void addRecordValue(IValueSpecification valueSpecification, String value) {
        recordValues.add(new FDOValue(value, valueSpecification));
    }
}
