package io.github.lipiridi.searchengine.entity.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LengthClass {
    MILLIMETER("mm", 1),
    CENTIMETER("cm", 10),
    METER("m", 1000);

    private final String symbol;
    private final long quantityInBaseUnits;
}
