package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SubSchemaRelation {
    denyAdditionalProperties("denyAdditionalProperties"),
    allowAdditionalProperties("allowAdditionalProperties"),
    requireAllProperties("requireAllProperties"),
    requireAnyOfProperties("requireAnyOfProperties"),
    requireOneOfProperties("requireOneOfProperties"),
    requireNoneOfProperties("requireNoneOfProperties");
    private final String name;
}
