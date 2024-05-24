package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Node("IDORISEntity")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class GenericIDORISEntity implements Serializable {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    String pid;

    @Version
    Long version;

    @CreatedDate
    Instant createdAt;

    @LastModifiedDate
    Instant lastModifiedAt;

    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    Set<User> contributors;

    @Relationship(value = "license", direction = Relationship.Direction.OUTGOING)
    License license;
}
