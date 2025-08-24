package io.github.lipiridi.searchengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SearchRequest(
        @NotNull @Min(1) Integer page,
        @NotNull @Min(1) Integer size,
        List<@Valid Filter> filters,
        List<@Valid Sort> sorts,
        boolean withoutTotals) {

    public SearchRequest(@NotNull @Min(1) Integer page, @NotNull @Min(1) Integer size) {
        this(page, size, null, null, false);
    }

    public SearchRequest(@NotNull @Min(1) Integer page, @NotNull @Min(1) Integer size, @Valid Filter filter) {
        this(page, size, List.of(filter), null, false);
    }

    public SearchRequest(@NotNull @Min(1) Integer page, @NotNull @Min(1) Integer size, @Valid Sort sort) {
        this(page, size, null, List.of(sort), false);
    }

    public SearchRequest(
            @NotNull @Min(1) Integer page,
            @NotNull @Min(1) Integer size,
            List<@Valid Filter> filters,
            List<@Valid Sort> sorts) {
        this(page, size, filters, sorts, false);
    }
}
