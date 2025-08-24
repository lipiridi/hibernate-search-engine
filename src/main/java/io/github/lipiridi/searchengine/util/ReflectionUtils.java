package io.github.lipiridi.searchengine.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
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
            Map.entry(Instant.class, value -> parseOrCastFunction(value, Instant::parse, Function.identity())),
            Map.entry(
                    LocalDate.class,
                    value -> parseOrCastFunction(value, LocalDate::parse, instant -> instant.atZone(ZoneOffset.UTC)
                            .toLocalDate())),
            Map.entry(
                    LocalDateTime.class,
                    value -> parseOrCastFunction(value, LocalDateTime::parse, instant -> instant.atZone(ZoneOffset.UTC)
                            .toLocalDateTime())),
            Map.entry(
                    ZonedDateTime.class,
                    value -> parseOrCastFunction(
                            value, ZonedDateTime::parse, instant -> instant.atZone(ZoneOffset.UTC))),
            Map.entry(
                    OffsetDateTime.class,
                    value -> parseOrCastFunction(
                            value, OffsetDateTime::parse, instant -> instant.atOffset(ZoneOffset.UTC))),
            Map.entry(Currency.class, value -> Currency.getInstance(value.toUpperCase())));

    private ReflectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T extends Temporal> T parseOrCastFunction(
            String value, Function<String, T> defaultParse, Function<Instant, T> fallbackParse) {
        try {
            return defaultParse.apply(value);
        } catch (DateTimeParseException e) {
            return fallbackParse.apply(parseEpochOrFractional(value));
        }
    }

    public static Instant parseEpochOrFractional(String input) {
        // 2. Pure number (epoch millis)
        if (input.matches("^-?\\d+$")) {
            long millis = Long.parseLong(input);
            return Instant.ofEpochMilli(millis);
        }

        // 3. Fractional number
        if (input.matches("^-?\\d+\\.\\d+$")) {
            BigDecimal value = new BigDecimal(input);

            // Separate integer and fraction
            BigDecimal intPart = value.setScale(0, RoundingMode.DOWN);
            BigDecimal fracPart = value.subtract(intPart);

            long intVal = intPart.longValueExact();
            int nanos = fracPart.movePointRight(9).intValue(); // fraction → nanos

            if (Math.abs(intVal) >= 1_000_000_000_000L) {
                // Treat as millis + nanos
                long seconds = intVal / 1000;
                int millisRemainder = (int) (intVal % 1000);
                nanos += millisRemainder * 1_000_000;
                return Instant.ofEpochSecond(seconds, nanos);
            } else {
                // Treat as seconds + nanos
                return Instant.ofEpochSecond(intVal, nanos);
            }
        }

        throw new IllegalArgumentException("Unsupported timestamp format: " + input);
    }

    public static Class<?> getPrimitiveWrapper(Class<?> fieldType) {
        if (!fieldType.isPrimitive()) {
            return fieldType;
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
            default -> throw new IllegalArgumentException("Unsupported primitive type: " + fieldType.getTypeName());
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
