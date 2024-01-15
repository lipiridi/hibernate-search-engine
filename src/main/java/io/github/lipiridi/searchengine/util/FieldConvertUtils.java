package io.github.lipiridi.searchengine.util;

import static io.github.lipiridi.searchengine.util.ReflectionUtils.getCastClass;

import io.github.lipiridi.searchengine.FilterType;
import io.github.lipiridi.searchengine.HibernateSearchEngineException;
import io.github.lipiridi.searchengine.SearchField;
import io.github.lipiridi.searchengine.dto.Filter;
import jakarta.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FieldConvertUtils {

    private static final Map<Class<?>, Set<FilterType>> allowedFiltersByClass = Arrays.stream(FilterType.values())
            .flatMap(filterType -> filterType.getSupportedClasses().stream()
                    .map(clazz -> new AbstractMap.SimpleEntry<>(clazz, filterType)))
            .collect(Collectors.groupingBy(
                    Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));

    private FieldConvertUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static SearchField resolveSearchField(Map<String, SearchField> searchFields, Filter filter) {
        SearchField searchField = searchFields.get(filter.field());
        if (!allowedFiltersByClass.get(getCastClass(searchField.fieldType())).contains(filter.type())) {
            throw new HibernateSearchEngineException("Not allowed filter type for field %s".formatted(filter.field()));
        }

        return searchField;
    }

    public static Object getConvertedValue(String originalValue, SearchField searchField) {
        Class<?> fieldType = searchField.fieldType();
        var convertFunction = convertFunction(fieldType);
        if (convertFunction == null) {
            throw new HibernateSearchEngineException(
                    "Unable to find convert function for field type %s".formatted(fieldType));
        }
        try {
            return convertFunction.apply(originalValue);
        } catch (Exception e) {
            throw new HibernateSearchEngineException(
                    "Unable to convert search field %s with value %s".formatted(searchField.id(), originalValue));
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Function<String, Object> convertFunction(Class<?> entityClass) {
        if (Enum.class.isAssignableFrom(entityClass)) {
            return value -> Enum.valueOf((Class<? extends Enum>) entityClass, value.toUpperCase());
        }

        return ReflectionUtils.CLASS_CAST_FUNCTIONS.get(entityClass);
    }
}
