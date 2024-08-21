package io.github.lipiridi.searchengine.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FilledFilterValueValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FilledFilterValue {

    String message() default "value is required for this filter type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
