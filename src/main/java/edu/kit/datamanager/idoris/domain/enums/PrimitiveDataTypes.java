package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PrimitiveDataTypes {
    string("string", String.class),
    number("number", Number.class),
    bool("boolean", Boolean.class);
    private final String jsonName;
    private final Class<?> javaClass;
}
