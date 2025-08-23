package io.github.lipiridi.searchengine.validation;

import io.github.lipiridi.searchengine.dto.Filter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FilledFilterValueValidator implements ConstraintValidator<FilledFilterValue, Filter> {

    @Override
    public boolean isValid(Filter filter, ConstraintValidatorContext context) {
        if (filter == null) {
            return true; // null objects are handled by @NotNull on the record fields
        }

        return filter.type().isNullAllowed() || filter.value() != null; // No need to check the value field
    }
}
