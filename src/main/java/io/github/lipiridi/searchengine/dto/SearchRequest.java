package io.github.lipiridi.searchengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SearchRequest(
        @Min(1) int page,
        @Min(1) int size,
        boolean withoutTotals,
        List<@Valid @NotNull Sort> sorts,
        List<@Valid @NotNull Filter> filters) {}
