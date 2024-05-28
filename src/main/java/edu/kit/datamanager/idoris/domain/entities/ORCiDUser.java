package edu.kit.datamanager.idoris.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Node("ORCiDUser")
public class ORCiDUser extends User {
    @JsonProperty("orcid")
    private String orcid;
}
