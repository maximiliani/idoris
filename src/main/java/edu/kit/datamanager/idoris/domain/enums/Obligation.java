package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Obligation {
    Mandatory("Mandatory"),
    Optional("Optional");
    private final String name;
}
