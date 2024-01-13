package io.github.lipiridi.searchengine;

public record SearchFieldData(String id, String path, Class<?> fieldClass) {

    public SearchFieldData(String id, Class<?> fieldClass) {
        this(id, id, fieldClass);
    }
}
