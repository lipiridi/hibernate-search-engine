package io.github.lipiridi.searchengine.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class ReflectionUtils {

    public static final Map<Class<?>, Function<String, Object>> CLASS_CAST_FUNCTIONS = Map.ofEntries(
            Map.entry(String.class, value -> value),
            Map.entry(UUID.class, UUID::fromString),
            Map.entry(Boolean.class, Boolean::parseBoolean),
            Map.entry(Byte.class, Byte::parseByte),
            Map.entry(Short.class, Short::parseShort),
            Map.entry(Integer.class, Integer::parseInt),
            Map.entry(Long.class, Long::parseLong),
            Map.entry(Double.class, Double::parseDouble),
            Map.entry(Float.class, Float::parseFloat),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(Instant.class, value -> Instant.ofEpochMilli(Long.parseLong(value))),
            Map.entry(Currency.class, value -> Currency.getInstance(value.toUpperCase())));

    private ReflectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Class<?> getFieldTypeWrapper(Class<?> fieldType) {
        if (!fieldType.isPrimitive()) {
            return getCastClass(fieldType);
        }

        return switch (fieldType.getTypeName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            default -> null;
        };
    }

    public static Class<?> getCastClass(Class<?> originalClass) {
        if (Enum.class.isAssignableFrom(originalClass)) {
            return Enum.class;
        }

        return originalClass;
    }

    public static Class<?> getGenericType(Field field) {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        if (genericType.getActualTypeArguments().length > 0) {
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }

        return null;
    }
}
