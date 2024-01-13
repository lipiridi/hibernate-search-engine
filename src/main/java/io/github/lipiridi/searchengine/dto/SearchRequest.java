package io.github.lipiridi.searchengine.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;

public record SearchRequest(
        @Min(1) int page,
        @Min(1) int size,
        @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid Sort> sorts,
        @JsonSetter(nulls = Nulls.AS_EMPTY) List<@Valid Filter> filters) {}
