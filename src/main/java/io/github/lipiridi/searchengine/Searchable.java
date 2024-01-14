package io.github.lipiridi.searchengine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Searchable {

    /**
     * Name of the field for the search request
     */
    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";
}
