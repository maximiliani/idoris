package edu.kit.datamanager.idoris.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@AllArgsConstructor
@Node("TextUser")
public class TextUser extends User {
    private String name;
    private String email;
    private String details;
}
