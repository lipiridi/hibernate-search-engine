package io.github.lipiridi.searchengine.dto;

import java.util.List;

public record SearchResponse<T>(int page, int size, int elements, int totalElements, List<T> data) {

    public SearchResponse(SearchRequest searchRequest, int elements, int totalElements, List<T> data) {
        this(searchRequest.page(), searchRequest.size(), elements, totalElements, data);
    }
}
