package io.github.lipiridi.searchengine.util;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FilterUtils {

    public static List<Class<?>> getCommonSupportedClasses() {
        List<Class<?>> classes = new ArrayList<>(List.of(String.class, UUID.class, Currency.class, Enum.class));
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
