package io.github.lipiridi.searchengine.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ReflectionUtils {

    public static Class<?> getFieldTypeWrapper(Class<?> fieldType) {
        if (!fieldType.isPrimitive()) {
            return getConversionClass(fieldType);
        }

        String typeName = fieldType.getTypeName();

        return switch (typeName) {
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

    public static Class<?> getConversionClass(Class<?> originalClass) {
        if (Enum.class.isAssignableFrom(originalClass)) {
            return Enum.class;
        }

        return originalClass;
    }
}
