package io.github.lipiridi.searchengine.dto;

import io.github.lipiridi.searchengine.FilterType;
import io.github.lipiridi.searchengine.validation.FilledFilterValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;

@FilledFilterValue
public record Filter(@NotBlank String field, @NotNull FilterType type, @Size(min = 1) Set<@NotBlank String> value) {

    public Filter(String field, FilterType type, String value) {
        this(field, type, Set.of(value));
    }
}
