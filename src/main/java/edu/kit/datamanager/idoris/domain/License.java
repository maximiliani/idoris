package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.io.Serializable;

@Node("License")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class License implements Serializable {
    @Id
    @GeneratedValue
    private String pid;
    private String name;
    private String url;
}
