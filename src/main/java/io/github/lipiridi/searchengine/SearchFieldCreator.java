package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.config.SearchEngineProperties;
import io.github.lipiridi.searchengine.util.ReflectionUtils;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchFieldCreator {

    private static final Set<Class<?>> SUPPORTED_CLASSES;
    private final SearchEngineProperties.NamingConvention namingConvention;

    private final Map<Class<?>, List<SearchField>> collectedSearchFields = new HashMap<>();

    static {
        var copy = new HashSet<>(ReflectionUtils.CLASS_CAST_FUNCTIONS.keySet());
        copy.add(Enum.class);
        SUPPORTED_CLASSES = Collections.unmodifiableSet(copy);
    }

    public SearchFieldCreator(SearchEngineProperties.NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    public Collection<SearchField> createFromClass(Class<?> entityClass) {
        List<SearchField> existingSearchFields = collectedSearchFields.get(entityClass);
        if (existingSearchFields != null) {
            return existingSearchFields;
        }

        List<SearchField> searchFieldList = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Searchable.class)) {
                Searchable searchableAnnotation = field.getAnnotation(Searchable.class);
                String fieldName = field.getName();
                String id = searchableAnnotation.value().isEmpty() ? fieldName : searchableAnnotation.value();
                Class<?> fieldTypeWrapper = ReflectionUtils.getFieldTypeWrapper(field.getType());

                // Prevent stack overflow
                if (fieldTypeWrapper.equals(entityClass)) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(fieldTypeWrapper)) {
                    Class<?> genericType = ReflectionUtils.getGenericType(field);
                    if (genericType == null) {
                        continue;
                    }

                    if (field.isAnnotationPresent(ElementCollection.class)
                            && SUPPORTED_CLASSES.contains(ReflectionUtils.getFieldTypeWrapper(genericType))) {
                        searchFieldList.add(new SearchField(formatId(id), fieldName, genericType, true));
                    } else if (field.isAnnotationPresent(OneToMany.class)) {
                        searchFieldList.addAll(createNestedEntitySearchFields(id, fieldName, genericType));
                    }
                } else {
                    if (SUPPORTED_CLASSES.contains(fieldTypeWrapper)) {
                        searchFieldList.add(new SearchField(formatId(id), fieldName, fieldTypeWrapper));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        searchFieldList.addAll(createNestedEntitySearchFields(id, fieldName, fieldTypeWrapper));
                    }
                }
            }
        }

        collectedSearchFields.put(entityClass, searchFieldList);
        return searchFieldList;
    }

    private List<SearchField> createNestedEntitySearchFields(String id, String fieldName, Class<?> fieldType) {
        return createFromClass(fieldType).stream()
                .map(nestedFieldData -> new SearchField(
                        formatId(id + capitalize(nestedFieldData.id())),
                        fieldName + "." + nestedFieldData.path(),
                        nestedFieldData.fieldType()))
                .collect(Collectors.toList());
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private String formatId(String id) {
        return switch (namingConvention) {
            case CAMEL_CASE -> id;
            case SNAKE_CASE -> id.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
            case DOT_CASE -> id.replaceAll("([a-z0-9])([A-Z])", "$1.$2").toLowerCase();
        };
    }
}
