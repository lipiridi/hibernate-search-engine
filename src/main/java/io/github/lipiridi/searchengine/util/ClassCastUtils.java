package io.github.lipiridi.searchengine.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassCastUtils {

    public static final Map<Class<?>, Function<String, Object>> CLASS_CAST_FUNCTIONS = Map.ofEntries(
            Map.entry(String.class, value -> value),
            Map.entry(UUID.class, UUID::fromString),
            Map.entry(Boolean.class, ClassCastUtils::toBoolean),
            Map.entry(Byte.class, ClassCastUtils::toByte),
            Map.entry(Short.class, ClassCastUtils::toShort),
            Map.entry(Integer.class, ClassCastUtils::toInteger),
            Map.entry(Long.class, ClassCastUtils::toLong),
            Map.entry(Double.class, ClassCastUtils::toDouble),
            Map.entry(Float.class, ClassCastUtils::toFloat),
            Map.entry(BigDecimal.class, ClassCastUtils::toBigDecimal),
            Map.entry(Instant.class, ClassCastUtils::toInstant),
            Map.entry(Currency.class, ClassCastUtils::toCurrency));

    public static boolean toBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    public static byte toByte(String value) {
        return Byte.parseByte(value);
    }

    public static short toShort(String value) {
        return Short.parseShort(value);
    }

    public static int toInteger(String value) {
        return Integer.parseInt(value);
    }

    public static long toLong(String value) {
        return Long.parseLong(value);
    }

    public static double toDouble(String value) {
        return Double.parseDouble(value);
    }

    public static float toFloat(String value) {
        return Float.parseFloat(value);
    }

    public static BigDecimal toBigDecimal(String value) {
        return new BigDecimal(value);
    }

    public static Instant toInstant(String value) {
        return Instant.ofEpochMilli(toLong(value));
    }

    public static Currency toCurrency(String value) {
        return Currency.getInstance(value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Enum toEnum(String value, Class<? extends Enum> enumClass) {
        return Enum.valueOf(enumClass, value.toUpperCase());
    }
}
