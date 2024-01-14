package io.github.lipiridi.searchengine.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public final class FilterUtils {

    private FilterUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<Class<?>> getCommonSupportedClasses() {
        List<Class<?>> classes =
                new ArrayList<>(List.of(String.class, Boolean.class, UUID.class, Currency.class, Enum.class));
        classes.addAll(getComparableSupportedClasses());
        return Collections.unmodifiableList(classes);
    }

    public static List<Class<?>> getComparableSupportedClasses() {
        return List.of(
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Double.class,
                Float.class,
                BigDecimal.class,
                Instant.class);
    }
}
