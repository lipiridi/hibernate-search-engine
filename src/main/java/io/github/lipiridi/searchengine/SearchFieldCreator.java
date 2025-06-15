package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.config.SearchEngineProperties;
import io.github.lipiridi.searchengine.util.ReflectionUtils;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public List<SearchField> createFromClass(Class<?> entityClass) {
        List<SearchField> existingSearchFields = collectedSearchFields.get(entityClass);
        if (existingSearchFields != null) {
            return existingSearchFields;
        }

        List<SearchField> searchFields = createFromClass(entityClass, null);

        collectedSearchFields.put(entityClass, searchFields);
        return searchFields;
    }

    public Map<Class<?>, List<SearchField>> getCollectedSearchFields() {
        return Collections.unmodifiableMap(collectedSearchFields);
    }

    private List<SearchField> createFromClass(Class<?> entityClass, @Nullable Class<?> parentClass) {
        List<SearchField> searchFields = new ArrayList<>();

        // Include search fields from the abstract class annotated with @MappedSuperclass
        Class<?> superClass = entityClass.getSuperclass();
        if (superClass != null && superClass.isAnnotationPresent(MappedSuperclass.class)) {
            searchFields.addAll(createFromClass(superClass, entityClass));
        }

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Searchable.class)) {
                Searchable searchableAnnotation = field.getAnnotation(Searchable.class);
                String fieldName = field.getName();
                String id = Objects.requireNonNull(searchableAnnotation).value().isEmpty()
                        ? namingConvention.formatId(fieldName)
                        : searchableAnnotation.value();
                Set<FilterType> filterTypes =
                        Arrays.stream(searchableAnnotation.filterTypes()).collect(Collectors.toSet());
                Class<?> fieldTypeWrapper = ReflectionUtils.getPrimitiveWrapper(field.getType());

                // Prevent stack overflow
                if (fieldTypeWrapper.equals(entityClass) || fieldTypeWrapper.equals(parentClass)) {
                    continue;
                }

                if (Collection.class.isAssignableFrom(fieldTypeWrapper)) {
                    Class<?> genericType = ReflectionUtils.getGenericType(field);
                    if (genericType == null || genericType.equals(parentClass)) {
                        continue;
                    }

                    if (field.isAnnotationPresent(ElementCollection.class)
                            && SUPPORTED_CLASSES.contains(ReflectionUtils.getCastClass(genericType))) {
                        searchFields.add(new SearchField(id, fieldName, genericType, true, true, filterTypes));
                    } else if (field.isAnnotationPresent(OneToMany.class)
                            || field.isAnnotationPresent(ManyToMany.class)) {
                        searchFields.addAll(
                                createNestedEntitySearchFields(id, fieldName, genericType, true, entityClass));
                    }
                } else {
                    if (SUPPORTED_CLASSES.contains(ReflectionUtils.getCastClass(fieldTypeWrapper))) {
                        searchFields.add(new SearchField(id, fieldName, fieldTypeWrapper, false, filterTypes));
                    } else if (field.isAnnotationPresent(ManyToOne.class)
                            || field.isAnnotationPresent(OneToOne.class)) {
                        searchFields.addAll(
                                createNestedEntitySearchFields(id, fieldName, fieldTypeWrapper, false, entityClass));
                    }
                }
            }
        }

        return searchFields;
    }

    private List<SearchField> createNestedEntitySearchFields(
            String id, String fieldName, Class<?> fieldType, boolean distinct, Class<?> parentClass) {
        return createFromClass(fieldType, parentClass).stream()
                .map(nestedSearchField -> new SearchField(
                        namingConvention.mergeStrings(id, namingConvention.formatId(nestedSearchField.id())),
                        fieldName + "." + nestedSearchField.path(),
                        nestedSearchField.fieldType(),
                        nestedSearchField.elementCollection(),
                        distinct,
                        nestedSearchField.filterTypes()))
                .collect(Collectors.toList());
    }
}
