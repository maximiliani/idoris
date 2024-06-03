package edu.kit.datamanager.idoris.domain.entities;

import edu.kit.datamanager.idoris.domain.enums.PrimitiveDataTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.Set;

@Node("BasicDataType")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasicDataType extends DataType {
    @Relationship(value = "inheritsFrom", direction = Relationship.Direction.OUTGOING)
    private BasicDataType inheritsFrom;

    private PrimitiveDataTypes primitiveDataType;
    private Category category = Category.Format;
    private String unitName;
    private String unitSymbol;
    private String definedBy;
    private String standard_uncertainty;
    private String restrictions;
    private String regex;
    private String regexFlavour = "ecma-262-RegExp";

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

//    @Projection(name = "full", types = BasicDataType.class)
//    public interface FullProjection {
//        String getPid();
//
//        Instant getCreatedAt();
//
//        Set<BasicDataType> getInheritsFrom();
//
//        Set<User> getContributors();
//
//        License getLicense();
//
//        Instant getLastModifiedAt();
//
//        Long getVersion();
//
//        String getName();
//
//        String getDescription();
//
//        List<String> getExpectedUses();
//
//        PrimitiveDataTypes getPrimitiveDataType();
//
//        Category getCategory();
//
//        String getUnitName();
//
//        String getUnitSymbol();
//
//        String getDefinedBy();
//
//        String getStandard_uncertainty();
//
//        String getRestrictions();
//
//        String getRegex();
//
//        String getRegexFlavour();
//
//        String getDefaultValue();
//
//        String[] getValueEnum();
//    }
}
