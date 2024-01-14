package io.github.lipiridi.searchengine;

public record SearchField(String id, String path, Class<?> fieldType) {

    public SearchField(String id, Class<?> fieldType) {
        this(id, id, fieldType);
    }
}
