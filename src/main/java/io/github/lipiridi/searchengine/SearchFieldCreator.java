package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.util.ReflectionUtils;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchFieldCreator {

    private static final Set<Class<?>> SUPPORTED_CLASSES;

    private final Map<Class<?>, List<SearchField>> collectedSearchFields = new HashMap<>();

    static {
        var copy = new HashSet<>(ReflectionUtils.CLASS_CAST_FUNCTIONS.keySet());
        copy.add(Enum.class);
        SUPPORTED_CLASSES = Collections.unmodifiableSet(copy);
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

                if (Collection.class.isAssignableFrom(fieldTypeWrapper)) {
                    Class<?> genericType = getGenericType(field);
                    if (genericType == null) {
                        continue;
                    }

                    if (field.isAnnotationPresent(ElementCollection.class)
                            && SUPPORTED_CLASSES.contains(ReflectionUtils.getFieldTypeWrapper(genericType))) {
                        searchFieldList.add(new SearchField(id, fieldName, genericType, true));
                    } else if (field.isAnnotationPresent(OneToMany.class)) {
                        // Handle nested entities within collections
                        searchFieldList.addAll(createFromClass(genericType).stream()
                                .map(nestedFieldData -> new SearchField(
                                        id + nestedFieldData.id(),
                                        fieldName + "." + nestedFieldData.path(),
                                        nestedFieldData.fieldType()))
                                .toList());
                    }
                } else {
                    if (SUPPORTED_CLASSES.contains(fieldTypeWrapper)) {
                        searchFieldList.add(new SearchField(id, fieldName, fieldTypeWrapper));
                    } else if (field.isAnnotationPresent(ManyToOne.class)) {
                        // Handle nested entities
                        searchFieldList.addAll(createFromClass(fieldTypeWrapper).stream()
                                .map(nestedFieldData -> new SearchField(
                                        id + nestedFieldData.id(),
                                        fieldName + "." + nestedFieldData.path(),
                                        nestedFieldData.fieldType()))
                                .toList());
                    }
                }
            }
        }

        collectedSearchFields.put(entityClass, searchFieldList);
        return searchFieldList;
    }

    private Class<?> getGenericType(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        if (genericType.getActualTypeArguments().length > 0) {
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return null;
    }
}
