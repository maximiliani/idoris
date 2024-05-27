package edu.kit.datamanager.idoris.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
//@Node("StandardIssuer")
public enum StandardIssuer {
    ISO("ISO"),
    W3C("W3C"),
    ITU("ITU"),
    RFC("RFC"),
    DTR("DTR"),
    Other("other");
    private final String name;
}
