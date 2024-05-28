package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import edu.kit.datamanager.idoris.domain.enums.PrimitiveDataTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@AllArgsConstructor
@RequiredArgsConstructor
public class DataType extends GenericIDORISEntity {
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private Set<DataType> inheritsFrom;

    private String name;
    private String description;
    private List<String> expectedUses;
    private PrimitiveDataTypes primitiveDataType;
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
    private Set<String> valueEnum;

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

        PrimitiveDataTypes getPrimitiveDataType();

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
