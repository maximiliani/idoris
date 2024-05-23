package edu.kit.datamanager.idoris.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.List;

@Node("Operation")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
public class Operation {
    @Id
    private String pid;

    @CreatedDate
    private Instant createdAt;
    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    private List<Person> contributors;
    @Relationship(value = "license", direction = Relationship.Direction.OUTGOING)
    private License license;


    @Relationship(value = "executableOn", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> environment;
    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> execution;
    @LastModifiedDate
    private Instant lastModifiedAt;
    @Version
    private Long version;

    private String name;
    private String description;
}
