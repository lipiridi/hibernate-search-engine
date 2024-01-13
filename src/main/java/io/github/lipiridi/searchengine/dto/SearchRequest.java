package io.github.lipiridi.searchengine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;

public record SearchRequest(@Min(1) int page, @Min(1) int size, List<@Valid Sort> sorts, List<@Valid Filter> filters) {}
