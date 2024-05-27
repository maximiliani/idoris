package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public enum NatureOfApplicability implements Serializable, Comparable<NatureOfApplicability> {
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
