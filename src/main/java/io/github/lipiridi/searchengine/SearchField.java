package io.github.lipiridi.searchengine;

public record SearchField(String id, String path, Class<?> fieldType, boolean elementCollection) {

    public SearchField(String id, Class<?> fieldType) {
        this(id, id, fieldType);
    }

    public SearchField(String id, String path, Class<?> fieldType) {
        this(id, path, fieldType, false);
    }
}
