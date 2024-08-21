package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.util.FilterUtils;
import java.util.List;

public enum FilterType {
    IS_NULL(FilterUtils.getCommonSupportedClasses(), true),
    IS_NOT_NULL(FilterUtils.getCommonSupportedClasses(), true),
    EQUAL(FilterUtils.getCommonSupportedClasses()),
    NOT_EQUAL(FilterUtils.getCommonSupportedClasses()),
    IN(FilterUtils.getCommonSupportedClasses()),
    NOT_IN(FilterUtils.getCommonSupportedClasses()),
    LIKE(List.of(String.class)),
    NOT_LIKE(List.of(String.class)),
    GREATER_THAN(FilterUtils.getComparableSupportedClasses()),
    LESS_THAN(FilterUtils.getComparableSupportedClasses()),
    GREATER_THAN_OR_EQUAL(FilterUtils.getComparableSupportedClasses()),
    LESS_THAN_OR_EQUAL(FilterUtils.getComparableSupportedClasses());

    private final List<Class<?>> supportedClasses;
    private final boolean nullAllowed;

    FilterType(List<Class<?>> supportedClasses) {
        this(supportedClasses, false);
    }

    FilterType(List<Class<?>> supportedClasses, boolean nullAllowed) {
        this.supportedClasses = supportedClasses;
        this.nullAllowed = nullAllowed;
    }

    public List<Class<?>> getSupportedClasses() {
        return this.supportedClasses;
    }

    public boolean isNullAllowed() {
        return nullAllowed;
    }
}
