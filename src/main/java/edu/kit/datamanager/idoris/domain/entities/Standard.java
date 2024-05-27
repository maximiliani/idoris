package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.enums.NatureOfApplicability;
import edu.kit.datamanager.idoris.domain.enums.StandardIssuer;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Standard")
@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class Standard {
    @Id
    @GeneratedValue
    private String pid;

    private String name;
    private StandardIssuer issuer;
    private String details;
    private NatureOfApplicability natureOfApplicability;
}

