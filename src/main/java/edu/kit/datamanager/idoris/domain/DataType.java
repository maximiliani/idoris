package edu.kit.datamanager.idoris.domain;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.neo4j.core.schema.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Node("DataType")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@With
public class DataType implements IValueSpecification {
    @Id
    @GeneratedValue
    private String pid;

    @CreatedDate
    private Instant createdAt;
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<DataType> inheritsFrom;
    @Relationship(value = "contributors", direction = Relationship.Direction.OUTGOING)
    private Set<Person> contributors;
    @Relationship(value = "standards", direction = Relationship.Direction.OUTGOING)
    private Set<Standard> standards;
    @Relationship(value = "license", direction = Relationship.Direction.OUTGOING)
    private License license;
    @LastModifiedDate
    private Instant lastModifiedAt;
    @Version
    private Long version;

    private String name;
    private String description;
    private List<String> expectedUses;
    private PrimitiveDataType primitiveDataType;
    private Category category = Category.Format;
    private String unitName;
    private String unitSymbol;
    private String definedBy;
    private String standard_uncertainty;
    private String restrictions;
    private String regex;
    private String regexFlavour = "ecma-262-RegExp";

    @Property("default")
    private String defaultValue;

    @Property("enum")
    private String[] valueEnum;

    public void addInheritsFrom(DataType superType) {
        inheritsFrom.add(superType);
    }

    public void addContributor(Person contributor) {
        contributors.add(contributor);
    }

    public void addStandard(Standard standard) {
        standards.add(standard);
    }

    public void removeInheritsFrom(DataType superType) {
        inheritsFrom.remove(superType);
    }

    public void removeContributor(Person contributor) {
        contributors.remove(contributor);
    }

    public void removeStandard(Standard standard) {
        standards.remove(standard);
    }

    @AllArgsConstructor
    @Getter
    public enum PrimitiveDataType {
        string("string", String.class),
        number("number", Number.class),
        bool("boolean", Boolean.class);
        private final String jsonName;
        private final Class<?> javaClass;
    }

    @AllArgsConstructor
    @Getter
    public enum Category {
        MeasurementUnit("Measurement Unit"),
        Format("Format"),
        CharacterSet("Character Set"),
        Encoding("Encoding"),
        Other("Other");

        private final String name;
    }
}
