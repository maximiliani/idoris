package edu.kit.datamanager.idoris.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;

@Node("OperationStep")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class OperationStep implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String title;
    private ExecutionMode mode = ExecutionMode.sync;

    @Relationship(value = "steps", direction = Relationship.Direction.OUTGOING)
    private List<OperationStep> steps;

    @Relationship(value = "operation", direction = Relationship.Direction.OUTGOING)
    private Operation operation;

    @Relationship(value = "operationTypeProfile", direction = Relationship.Direction.OUTGOING)
    private OperationTypeProfile operationTypeProfile;

    @Relationship(value = "attributes", direction = Relationship.Direction.INCOMING)
    private List<AttributeMapping> attributes;

    @Relationship(value = "outputs", direction = Relationship.Direction.OUTGOING)
    private List<AttributeMapping> outputs;

    public enum ExecutionMode {
        sync,
        async
    }

}
