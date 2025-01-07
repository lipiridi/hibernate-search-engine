package io.github.lipiridi.searchengine;

import jakarta.annotation.Nullable;
import java.util.Set;

public record SearchField(
        String id,
        String path,
        Class<?> fieldType,
        boolean elementCollection,
        boolean distinct,
        // Leave null in order to support all filters supported by field type
        @Nullable Set<FilterType> filterTypes) {

    public SearchField(String id, Class<?> fieldType, boolean distinct) {
        this(id, id, fieldType, distinct);
    }

    public SearchField(String id, Class<?> fieldType, boolean elementCollection, boolean distinct) {
        this(id, id, fieldType, elementCollection, distinct);
    }

    public SearchField(String id, Class<?> fieldType, boolean distinct, Set<FilterType> filterTypes) {
        this(id, id, fieldType, false, distinct, filterTypes);
    }

    public SearchField(String id, String path, Class<?> fieldType, boolean distinct) {
        this(id, path, fieldType, false, distinct);
    }

    public SearchField(String id, String path, Class<?> fieldType, boolean distinct, Set<FilterType> filterTypes) {
        this(id, path, fieldType, false, distinct, filterTypes);
    }

    public SearchField(String id, String path, Class<?> fieldType, boolean elementCollection, boolean distinct) {
        this(id, path, fieldType, elementCollection, distinct, null);
    }
}
