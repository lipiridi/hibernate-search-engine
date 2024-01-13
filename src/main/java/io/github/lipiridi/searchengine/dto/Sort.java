package io.github.lipiridi.searchengine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.query.SortDirection;

public record Sort(@NotBlank String field, @NotNull SortDirection order) {}
