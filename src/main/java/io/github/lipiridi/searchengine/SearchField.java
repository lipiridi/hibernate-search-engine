package io.github.lipiridi.searchengine;

import jakarta.annotation.Nullable;
import java.util.Set;

public record SearchField(
        String id,
        String path,
        Class<?> fieldType,
        boolean elementCollection,
        // Leave null in order to support all filters supported by field type
        @Nullable Set<FilterType> filterTypes) {

    public SearchField(String id, Class<?> fieldType) {
        this(id, id, fieldType);
    }

    public SearchField(String id, Class<?> fieldType, boolean elementCollection) {
        this(id, id, fieldType, elementCollection);
    }

    public SearchField(String id, Class<?> fieldType, Set<FilterType> filterTypes) {
        this(id, id, fieldType, false, filterTypes);
    }

    public SearchField(String id, String path, Class<?> fieldType) {
        this(id, path, fieldType, false);
    }

    public SearchField(String id, String path, Class<?> fieldType, Set<FilterType> filterTypes) {
        this(id, path, fieldType, false, filterTypes);
    }

    public SearchField(String id, String path, Class<?> fieldType, boolean elementCollection) {
        this(id, path, fieldType, elementCollection, null);
    }
}
