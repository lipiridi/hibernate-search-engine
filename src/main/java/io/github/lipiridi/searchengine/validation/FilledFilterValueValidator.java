package io.github.lipiridi.searchengine.validation;

import io.github.lipiridi.searchengine.FilterType;
import io.github.lipiridi.searchengine.dto.Filter;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FilledFilterValueValidator implements ConstraintValidator<FilledFilterValue, Filter> {

    @Override
    public boolean isValid(Filter filter, ConstraintValidatorContext context) {
        if (filter == null) {
            return true; // null objects are handled by @NotNull on the record fields
        }

        if (filter.type() == FilterType.IS_NULL || filter.type() == FilterType.IS_NOT_NULL) {
            return true; // No need to check the value field
        }

        return filter.value() != null;
    }
}
