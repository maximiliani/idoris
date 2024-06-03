package edu.kit.datamanager.idoris.domain.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.kit.datamanager.idoris.domain.GenericIDORISEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("DataType")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, property = "type")
@JsonSubTypes({
//        @JsonSubTypes.Type(value = BasicDataType.class, name = "DataType"),
        @JsonSubTypes.Type(value = BasicDataType.class, name = "BasicDataType"),
        @JsonSubTypes.Type(value = TypeProfile.class, name = "TypeProfile"),
})
public abstract class DataType extends GenericIDORISEntity {
    private String type;
    private String name;
    private String description;
    private List<String> expectedUses;

    @Property("default")
    private String defaultValue;
}
