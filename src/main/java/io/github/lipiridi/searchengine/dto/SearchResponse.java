package io.github.lipiridi.searchengine.dto;

import java.util.List;

public record SearchResponse<T>(int page, int size, int elements, long totalElements, List<T> data) {

    public SearchResponse(SearchRequest searchRequest, int elements, long totalElements, List<T> data) {
        this(searchRequest.page(), searchRequest.size(), elements, totalElements, data);
    }
}
