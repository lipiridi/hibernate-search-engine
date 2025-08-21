package io.github.lipiridi.searchengine.dto;

import jakarta.validation.Valid;
import java.util.List;

public record TotalElementsRequest(List<@Valid Filter> filters) {}
