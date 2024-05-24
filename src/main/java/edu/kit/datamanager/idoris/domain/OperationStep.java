package edu.kit.datamanager.idoris.domain;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Node("OperationStep")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
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
    private Map<String, String> attributes;
    private Map<String, String> outputs;

    public enum ExecutionMode {
        sync,
        async
    }
}
