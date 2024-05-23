package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

@Getter
@Setter
@AllArgsConstructor
public class PID {
    @GeneratedValue
    String value;
}
