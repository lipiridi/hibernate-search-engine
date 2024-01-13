package io.github.lipiridi.searchengine;

import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN;
import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN_OR_EQUAL;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN_OR_EQUAL;

import io.github.lipiridi.searchengine.config.SearchEngineProperties;
import io.github.lipiridi.searchengine.dto.Filter;
import io.github.lipiridi.searchengine.dto.SearchRequest;
import io.github.lipiridi.searchengine.dto.SearchResponse;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.SortDirection;

@RequiredArgsConstructor
public class SearchService {

    private final EntityManager entityManager;
    private final SearchEngineProperties searchEngineProperties;
    private final SearchFieldConverter searchFieldConverter = new SearchFieldConverter();
    private final SearchFieldCreator searchFieldCreator = new SearchFieldCreator();

    public <E> SearchResponse<E> search(SearchRequest searchRequest, Class<E> entityClass) {
        var searchFields = searchFieldCreator.createFromClass(entityClass);
        return search(searchRequest, entityClass, searchFields);
    }

    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest, Class<E> entityClass, @Nullable Function<E, M> mapper) {
        var searchFields = searchFieldCreator.createFromClass(entityClass);
        return search(searchRequest, entityClass, searchFields, mapper);
    }

    public <E> SearchResponse<E> search(
            SearchRequest searchRequest, Class<E> entityClass, Collection<SearchFieldData> searchFieldData) {
        return search(searchRequest, entityClass, searchFieldData, null);
    }

    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest,
            Class<E> entityClass,
            Collection<SearchFieldData> searchFieldData,
            @Nullable Function<E, M> mapper) {
        Map<String, SearchFieldData> searchFieldMap =
                searchFieldData.stream().collect(Collectors.toMap(SearchFieldData::id, Function.identity()));
        return search(searchRequest, entityClass, searchFieldMap, mapper);
    }

    public <E> SearchResponse<E> search(
            SearchRequest searchRequest, Class<E> entityClass, Map<String, SearchFieldData> searchFieldMap) {
        return search(searchRequest, entityClass, searchFieldMap, null);
    }

    @SuppressWarnings("unchecked")
    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest,
            Class<E> entityClass,
            Map<String, SearchFieldData> searchFieldMap,
            @Nullable Function<E, M> mapper) {
        List<E> entities = fetchEntities(searchRequest, searchFieldMap, entityClass);
        int totalNumber = totalNumber(searchRequest, searchFieldMap, entityClass);

        List<M> mappedEntities = mapper == null
                ? (List<M>) entities
                : entities.stream().map(mapper).toList();

        return new SearchResponse<>(searchRequest, entities.size(), totalNumber, mappedEntities);
    }

    public <E> List<E> fetchEntities(
            SearchRequest searchRequest, Map<String, SearchFieldData> searchFieldMap, Class<E> entityClass) {
        validateSearchRequest(searchRequest, searchFieldMap);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> root = criteriaQuery.from(entityClass);

        addFilters(searchFieldMap, searchRequest, criteriaBuilder, criteriaQuery, root);

        addSorts(searchRequest, criteriaBuilder, criteriaQuery, root);

        TypedQuery<E> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((searchRequest.page() - 1) * searchRequest.size());
        query.setMaxResults(searchRequest.size());

        return query.getResultList();
    }

    private <E> int totalNumber(
            SearchRequest searchRequest, Map<String, SearchFieldData> searchFieldMap, Class<E> entityClass) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<E> root = criteriaQuery.from(entityClass);

        criteriaQuery.select(criteriaBuilder.count(root));

        addFilters(searchFieldMap, searchRequest, criteriaBuilder, criteriaQuery, root);

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        return query.getSingleResult().intValue();
    }

    private void validateSearchRequest(SearchRequest searchRequest, Map<String, SearchFieldData> searchFieldMap) {
        int maxPageSize = searchEngineProperties.getMaxPageSize();
        if (searchRequest.size() > maxPageSize) {
            throw new DatabaseSearchEngineException(
                    "The search request is limited to %s results".formatted(maxPageSize));
        }

        searchRequest.sorts().forEach(sort -> validateExistingSearchField(searchFieldMap, sort.field()));
        searchRequest.filters().forEach(filter -> validateExistingSearchField(searchFieldMap, filter.field()));
    }

    private void validateExistingSearchField(Map<String, SearchFieldData> searchFieldMap, String field) {
        SearchFieldData searchFieldData = searchFieldMap.get(field);
        if (searchFieldData == null) {
            throw new DatabaseSearchEngineException("Search field '%s' was not found!".formatted(field));
        }
    }

    private void addFilters(
            Map<String, SearchFieldData> searchFieldMap,
            SearchRequest searchRequest,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root) {
        Predicate predicate = criteriaBuilder.conjunction();

        var filters = searchRequest.filters();
        if (filters.isEmpty()) {
            return;
        }

        var searchConsumer = new FilterQueryCriteriaConsumer(criteriaBuilder, root, searchFieldMap, predicate);
        filters.forEach(searchConsumer);

        criteriaQuery.where(searchConsumer.getPredicate());
    }

    private void addSorts(
            SearchRequest searchRequest,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root) {
        var sorts = searchRequest.sorts();
        if (sorts.isEmpty()) {
            return;
        }

        List<Order> orders = sorts.stream()
                .map(sort -> sort.order() == SortDirection.DESCENDING
                        ? criteriaBuilder.desc(getPath(root, sort.field()))
                        : criteriaBuilder.asc(getPath(root, sort.field())))
                .toList();

        criteriaQuery.orderBy(orders);
    }

    private <Y> Path<Y> getPath(Root<?> root, String stringPath) {
        String[] fields = stringPath.split("\\.");

        Path<Y> path = root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }

        return path;
    }

    @AllArgsConstructor
    private class FilterQueryCriteriaConsumer implements Consumer<Filter> {

        private final CriteriaBuilder builder;
        private final Root<?> root;
        private final Map<String, SearchFieldData> searchFields;

        @Getter
        private Predicate predicate;

        @Override
        public void accept(Filter filter) {
            SearchFieldData searchFieldData = searchFieldConverter.resolveSearchField(searchFields, filter);

            List<?> valueList = filter.value().stream()
                    .map(originalValue -> searchFieldConverter.getConvertedValue(originalValue, searchFieldData))
                    .toList();
            Object singleValue = valueList.getFirst();

            switch (filter.type()) {
                case EQUAL -> predicate = builder.and(predicate, builder.equal(getPath(searchFieldData), singleValue));
                case NOT_EQUAL -> predicate =
                        builder.and(predicate, builder.notEqual(getPath(searchFieldData), singleValue));
                case IN -> predicate =
                        builder.and(predicate, getPath(searchFieldData).in(valueList));
                case NOT_IN -> predicate = builder.and(
                        predicate, getPath(searchFieldData).in(valueList).not());
                case LIKE -> predicate =
                        builder.and(predicate, builder.like(getPath(searchFieldData), "%" + singleValue + "%"));
                case NOT_LIKE -> predicate =
                        builder.and(predicate, builder.notLike(getPath(searchFieldData), "%" + singleValue + "%"));
                case GREATER_THAN -> buildComparePredicate(GREATER_THAN, searchFieldData, singleValue);
                case GREATER_THAN_OR_EQUAL -> buildComparePredicate(
                        GREATER_THAN_OR_EQUAL, searchFieldData, singleValue);
                case LESS_THAN -> buildComparePredicate(LESS_THAN, searchFieldData, singleValue);
                case LESS_THAN_OR_EQUAL -> buildComparePredicate(LESS_THAN_OR_EQUAL, searchFieldData, singleValue);
            }
        }

        private void buildComparePredicate(FilterType filterType, SearchFieldData searchFieldData, Object value) {
            if (value instanceof Integer castedValue) {
                buildComparePredicate(filterType, searchFieldData, castedValue);
            }
            if (value instanceof Long castedValue) {
                buildComparePredicate(filterType, searchFieldData, castedValue);
            }
            if (value instanceof BigDecimal castedValue) {
                buildComparePredicate(filterType, searchFieldData, castedValue);
            }
            if (value instanceof Instant castedValue) {
                buildComparePredicate(filterType, searchFieldData, castedValue);
            }
        }

        private <K extends Comparable<K>> void buildComparePredicate(
                FilterType filterType, SearchFieldData searchFieldData, K value) {
            switch (filterType) {
                case GREATER_THAN -> predicate =
                        builder.and(predicate, builder.greaterThan(getPath(searchFieldData), value));
                case GREATER_THAN_OR_EQUAL -> predicate =
                        builder.and(predicate, builder.greaterThanOrEqualTo(getPath(searchFieldData), value));
                case LESS_THAN -> predicate = builder.and(predicate, builder.lessThan(getPath(searchFieldData), value));
                case LESS_THAN_OR_EQUAL -> predicate =
                        builder.and(predicate, builder.lessThanOrEqualTo(getPath(searchFieldData), value));
                default -> throw new DatabaseSearchEngineException(
                        String.format("Can't build compare predicate for filter type %s", filterType));
            }
        }

        private <Y> Path<Y> getPath(SearchFieldData searchFieldData) {
            return SearchService.this.getPath(root, searchFieldData.path());
        }
    }
}
