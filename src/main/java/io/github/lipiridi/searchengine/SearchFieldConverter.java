package io.github.lipiridi.searchengine;

import static io.github.lipiridi.searchengine.util.ReflectionUtils.getConversionClass;

import io.github.lipiridi.searchengine.dto.Filter;
import io.github.lipiridi.searchengine.util.ClassCastUtils;
import jakarta.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchFieldConverter {

    private final Map<Class<?>, Set<FilterType>> allowedFiltersByClass = Arrays.stream(FilterType.values())
            .flatMap(filterType -> filterType.getSupportedClasses().stream()
                    .map(clazz -> new AbstractMap.SimpleEntry<>(clazz, filterType)))
            .collect(Collectors.groupingBy(
                    Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));

    public SearchFieldData resolveSearchField(Map<String, SearchFieldData> searchFields, Filter filter) {
        SearchFieldData searchFieldData = searchFields.get(filter.field());
        if (!allowedFiltersByClass
                .get(getConversionClass(searchFieldData.fieldClass()))
                .contains(filter.type())) {
            throw new DatabaseSearchEngineException("Not allowed filter type for field %s".formatted(filter.field()));
        }

        return searchFieldData;
    }

    public Object getConvertedValue(String originalValue, SearchFieldData searchFieldData) {
        Class<?> entityClass = searchFieldData.fieldClass();
        Function<String, Object> convertFunction = convertFunction(entityClass);
        if (convertFunction == null) {
            throw new DatabaseSearchEngineException(
                    "Unable to find convert function for class %s".formatted(entityClass));
        }
        try {
            return convertFunction.apply(originalValue);
        } catch (Exception e) {
            throw new DatabaseSearchEngineException(
                    "Unable to convert search field %s with value %s".formatted(searchFieldData.id(), originalValue));
        }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Function<String, Object> convertFunction(Class<?> entityClass) {
        if (Enum.class.isAssignableFrom(entityClass)) {
            return value -> ClassCastUtils.toEnum(value, (Class<? extends Enum>) entityClass);
        }

        return ClassCastUtils.CLASS_CAST_FUNCTIONS.get(getConversionClass(entityClass));
    }
}
