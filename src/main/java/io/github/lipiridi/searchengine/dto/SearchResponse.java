package io.github.lipiridi.searchengine.dto;

import java.util.List;

public record SearchResponse<T>(int page, int size, int elements, Long totalElements, List<T> data) {

    public SearchResponse(SearchRequest searchRequest, int elements, Long totalElements, List<T> data) {
        this(searchRequest.page(), searchRequest.size(), elements, totalElements, data);
    }
}
