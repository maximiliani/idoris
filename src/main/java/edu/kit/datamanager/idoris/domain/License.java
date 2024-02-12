package edu.kit.datamanager.idoris.domain;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("License")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
public class License {
    @Id
    @GeneratedValue
    private String pid;
    private String name;
    private String url;

    public License(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public License(String url) {
        this.url = url;
    }
}
