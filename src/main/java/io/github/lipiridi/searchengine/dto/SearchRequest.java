package io.github.lipiridi.searchengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SearchRequest(
        @NotNull @Min(1) Integer page,
        @NotNull @Min(1) Integer size,
        List<@Valid Sort> sorts,
        List<@Valid Filter> filters,
        boolean withoutTotals) {}
