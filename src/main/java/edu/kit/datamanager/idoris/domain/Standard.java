package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Arrays;

@Node("Standard")
@Getter
@Setter
@ToString
public class Standard {
    @Id
    @GeneratedValue
    private String pid;

    private String name;
    private Issuer issuer;
    private String details;
    private NatureOfApplicability natureOfApplicability;

    @PersistenceCreator
    public Standard(String name, String issuer, String details, String natureOfApplicability) {
        this.name = name;
        this.issuer = Arrays.stream(Issuer.values()).filter(i -> i.name.equalsIgnoreCase(issuer)).findFirst().orElse(null);
        this.details = details;
        this.natureOfApplicability = Arrays.stream(NatureOfApplicability.values()).filter(n -> n.name.equalsIgnoreCase(natureOfApplicability)).findFirst().orElse(null);
    }

    @AllArgsConstructor
    @Getter
    public enum Issuer {
        ISO("ISO"),
        W3C("W3C"),
        ITU("ITU"),
        RFC("RFC"),
        DTR("DTR"),
        Other("other");
        private final String name;
    }

    @Getter
    @AllArgsConstructor
    public enum NatureOfApplicability {
        Extends("extends"),
        constrains("constrains"),
        specifies("specifies"),
        depends("depends"),
        previousVersion("is_previous_version_of"),
        newVersion("is_new_version_of"),
        semanticallyIdentical("is_semantically_identical"),
        semanticallySimilar("is_semantically_similar");
        private final String name;
    }

}
