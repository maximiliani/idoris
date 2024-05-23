package edu.kit.datamanager.idoris.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Node("Operation")
@Getter
@Setter
public class Operation extends GenericIDORISEntity {

    @Relationship(value = "executableOn", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> executableOn;
    @Relationship(value = "returns", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> returns;
    @Relationship(value = "environment", direction = Relationship.Direction.OUTGOING)
    private List<ValueSpecificationRelation> environment;
    @Relationship(value = "execution", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> execution;

    private String name;
    private String description;

    public Operation(PID pid, Long version, Instant createdAt, Instant lastModifiedAt, Set<User> contributors, License license, List<ValueSpecificationRelation> executableOn, List<ValueSpecificationRelation> returns, List<ValueSpecificationRelation> environment, List<OperationStep> execution, String name, String description) {
        super(pid, version, createdAt, lastModifiedAt, contributors, license);
        this.executableOn = executableOn;
        this.returns = returns;
        this.environment = environment;
        this.execution = execution;
        this.name = name;
        this.description = description;
    }
}
