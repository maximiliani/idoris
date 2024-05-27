package edu.kit.datamanager.idoris.domain.entities;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node("License")
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@RequiredArgsConstructor
public class License implements Serializable {
    @Id
    @GeneratedValue
    private String pid;
    private String name;
    private String url;

    public License(String url) {
        this.name = url;
        this.url = url;
    }
}
