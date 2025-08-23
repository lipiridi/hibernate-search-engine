package io.github.lipiridi.searchengine.entity.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeightClass {
    GRAM("g", 1),
    KILOGRAM("kg", 1000);

    private final String symbol;
    private final long quantityInBaseUnits;
}
