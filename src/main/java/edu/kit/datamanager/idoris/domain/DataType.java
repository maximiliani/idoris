package edu.kit.datamanager.idoris.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.rest.core.config.Projection;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Node("DataType")
@Getter
@Setter
public class DataType extends GenericIDORISEntity implements IValueSpecification {
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<DataType> inheritsFrom;

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

    public DataType(PID pid, Long version, Instant createdAt, Instant lastModifiedAt, Set<User> contributors, License license, Set<DataType> inheritsFrom, String name, String description, List<String> expectedUses, PrimitiveDataType primitiveDataType, Category category, String unitName, String unitSymbol, String definedBy, String standard_uncertainty, String restrictions, String regex, String regexFlavour, String defaultValue, String[] valueEnum) {
        super(pid, version, createdAt, lastModifiedAt, contributors, license);
        this.inheritsFrom = inheritsFrom;
        this.name = name;
        this.description = description;
        this.expectedUses = expectedUses;
        this.primitiveDataType = primitiveDataType;
        this.category = category;
        this.unitName = unitName;
        this.unitSymbol = unitSymbol;
        this.definedBy = definedBy;
        this.standard_uncertainty = standard_uncertainty;
        this.restrictions = restrictions;
        this.regex = regex;
        this.regexFlavour = regexFlavour;
        this.defaultValue = defaultValue;
        this.valueEnum = valueEnum;
    }

    public void addInheritsFrom(DataType superType) {
        inheritsFrom.add(superType);
    }

    public void removeInheritsFrom(DataType superType) {
        inheritsFrom.remove(superType);
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

    @Projection(name = "full", types = DataType.class)
    public interface FullProjection {
        String getPid();

        Instant getCreatedAt();

        Set<DataType> getInheritsFrom();

        Set<User> getContributors();

        License getLicense();

        Instant getLastModifiedAt();

        Long getVersion();

        String getName();

        String getDescription();

        List<String> getExpectedUses();

        PrimitiveDataType getPrimitiveDataType();

        Category getCategory();

        String getUnitName();

        String getUnitSymbol();

        String getDefinedBy();

        String getStandard_uncertainty();

        String getRestrictions();

        String getRegex();

        String getRegexFlavour();

        String getDefaultValue();

        String[] getValueEnum();
    }
}
