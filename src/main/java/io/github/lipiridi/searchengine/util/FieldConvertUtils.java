package io.github.lipiridi.searchengine.util;

import static io.github.lipiridi.searchengine.util.ReflectionUtils.getCastClass;

import io.github.lipiridi.searchengine.FilterType;
import io.github.lipiridi.searchengine.HibernateSearchEngineException;
import io.github.lipiridi.searchengine.SearchField;
import io.github.lipiridi.searchengine.dto.Filter;
import io.github.lipiridi.searchengine.dto.Sort;
import jakarta.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

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
        FilterType filterType = filter.type();
        Set<FilterType> allowedFilterTypes = searchField.filterTypes();

        Set<FilterType> existingFiltersByClass = allowedFiltersByClass.get(getCastClass(searchField.fieldType()));
        if (!existingFiltersByClass.contains(filterType)
                || (!CollectionUtils.isEmpty(allowedFilterTypes) && !allowedFilterTypes.contains(filterType))) {
            var availableFilters =
                    CollectionUtils.isEmpty(allowedFilterTypes) ? existingFiltersByClass : allowedFilterTypes;
            throw new HibernateSearchEngineException("Not allowed filter type for field %s. Available filters: %s"
                    .formatted(filter.field(), availableFilters));
        }

        if (!filterType.isNullAllowed() && filter.value() == null) {
            throw new HibernateSearchEngineException("Filter type '%s' requires a value. Invalid field: '%s'"
                    .formatted(filterType.name(), filter.field()));
        }

        return searchField;
    }

    public static SearchField resolveSearchField(Map<String, SearchField> searchFields, Sort sort) {
        SearchField searchField = searchFields.get(sort.field());

        if (searchField.distinct()) {
            throw new HibernateSearchEngineException(
                    "Sorting by fields in joined collections is not allowed. Invalid field: '%s'"
                            .formatted(sort.field()));
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
