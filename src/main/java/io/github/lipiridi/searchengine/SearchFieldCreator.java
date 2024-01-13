package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.util.ClassCastUtils;
import io.github.lipiridi.searchengine.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFieldCreator {

    private static final Set<Class<?>> SUPPORTED_CLASSES;

    static {
        var copy = new HashSet<>(ClassCastUtils.CLASS_CAST_FUNCTIONS.keySet());
        copy.add(Enum.class);
        SUPPORTED_CLASSES = Collections.unmodifiableSet(copy);
    }

    public Collection<SearchFieldData> createFromClass(Class<?> entityClass) {
        List<SearchFieldData> searchFieldDataList = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(SearchField.class)) {
                SearchField searchFieldAnnotation = field.getAnnotation(SearchField.class);
                String fieldName = field.getName();
                String id = searchFieldAnnotation.value().isEmpty() ? fieldName : searchFieldAnnotation.value();
                Class<?> fieldTypeWrapper = ReflectionUtils.getFieldTypeWrapper(field.getType());

                if (SUPPORTED_CLASSES.contains(fieldTypeWrapper)) {
                    searchFieldDataList.add(new SearchFieldData(id, fieldName, fieldTypeWrapper));
                } else if (Collection.class.isAssignableFrom(fieldTypeWrapper)) {
                    Class<?> genericType = getGenericType(field);
                    if (genericType != null
                            && SUPPORTED_CLASSES.contains(ReflectionUtils.getFieldTypeWrapper(genericType))) {
                        searchFieldDataList.add(new SearchFieldData(id, fieldName, genericType));
                    } else if (genericType != null) {
                        // Handle nested entities within collections
                        searchFieldDataList.addAll(createFromClass(genericType).stream()
                                .map(nestedFieldData -> new SearchFieldData(
                                        id + nestedFieldData.id(),
                                        fieldName + "." + nestedFieldData.path(),
                                        nestedFieldData.fieldClass()))
                                .toList());
                    }
                } else {
                    // Handle nested entities
                    searchFieldDataList.addAll(createFromClass(fieldTypeWrapper).stream()
                            .map(nestedFieldData -> new SearchFieldData(
                                    id + nestedFieldData.id(),
                                    fieldName + "." + nestedFieldData.path(),
                                    nestedFieldData.fieldClass()))
                            .toList());
                }
            }
        }

        return searchFieldDataList;
    }

    private Class<?> getGenericType(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        if (genericType.getActualTypeArguments().length > 0) {
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return null;
    }
}
