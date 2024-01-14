package io.github.lipiridi.searchengine.dto;

import java.util.List;

public record SearchResponse<T>(int page, int size, int items, int totalItems, List<T> data) {

    public SearchResponse(SearchRequest searchRequest, int number, int totalNumber, List<T> data) {
        this(searchRequest.page(), searchRequest.size(), number, totalNumber, data);
    }
}
