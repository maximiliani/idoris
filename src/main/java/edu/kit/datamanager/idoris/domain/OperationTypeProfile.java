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

@Node("OperationTypeProfile")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
public class OperationTypeProfile {
    @Id
    private String pid;

    @CreatedDate
    private Instant createdAt;
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private List<TypeProfile> inheritsFrom;
    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    private List<Person> contributors;
    @Relationship(value = "license", direction = Relationship.Direction.OUTGOING)
    private License license;
    @Relationship(value = "attributes", direction = Relationship.Direction.OUTGOING)
    private List<ProfileAttribute> attributes;
    @LastModifiedDate
    private Instant lastModifiedAt;
    @Version
    private Long version;

    private String name;
    private String description;
}
