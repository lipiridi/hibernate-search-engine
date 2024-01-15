package io.github.lipiridi.searchengine;

import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN;
import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN_OR_EQUAL;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN_OR_EQUAL;
import static io.github.lipiridi.searchengine.util.FieldConvertUtils.getConvertedValue;
import static io.github.lipiridi.searchengine.util.FieldConvertUtils.resolveSearchField;

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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.query.SortDirection;
import org.springframework.util.CollectionUtils;

public class SearchService {

    private final EntityManager entityManager;
    private final SearchEngineProperties searchEngineProperties;
    private final SearchFieldCreator searchFieldCreator;

    public SearchService(EntityManager entityManager, SearchEngineProperties searchEngineProperties) {
        this.entityManager = entityManager;
        this.searchEngineProperties = searchEngineProperties;

        searchFieldCreator = new SearchFieldCreator(searchEngineProperties.getNamingConvention());
    }

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
            SearchRequest searchRequest, Class<E> entityClass, Collection<SearchField> searchFields) {
        return search(searchRequest, entityClass, searchFields, null);
    }

    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest,
            Class<E> entityClass,
            Collection<SearchField> searchFields,
            @Nullable Function<E, M> mapper) {
        Map<String, SearchField> searchFieldMap =
                searchFields.stream().collect(Collectors.toMap(SearchField::id, Function.identity()));
        return search(searchRequest, entityClass, searchFieldMap, mapper);
    }

    public <E> SearchResponse<E> search(
            SearchRequest searchRequest, Class<E> entityClass, Map<String, SearchField> searchFieldMap) {
        return search(searchRequest, entityClass, searchFieldMap, null);
    }

    @SuppressWarnings("unchecked")
    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest,
            Class<E> entityClass,
            Map<String, SearchField> searchFieldMap,
            @Nullable Function<E, M> mapper) {
        List<E> entities = fetchEntities(searchRequest, searchFieldMap, entityClass);
        int totalNumber = totalElements(searchRequest, searchFieldMap, entityClass);

        List<M> mappedEntities = mapper == null
                ? (List<M>) entities
                : entities.stream().map(mapper).toList();

        return new SearchResponse<>(searchRequest, entities.size(), totalNumber, mappedEntities);
    }

    public <E> List<E> fetchEntities(
            SearchRequest searchRequest, Map<String, SearchField> searchFieldMap, Class<E> entityClass) {
        validateSearchRequest(searchRequest, searchFieldMap);

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> root = criteriaQuery.from(entityClass);

        addFilters(searchFieldMap, searchRequest, criteriaBuilder, criteriaQuery, root);
        addSorts(searchFieldMap, searchRequest, criteriaBuilder, criteriaQuery, root);

        TypedQuery<E> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((searchRequest.page() - 1) * searchRequest.size());
        query.setMaxResults(searchRequest.size());

        return query.getResultList();
    }

    private <E> int totalElements(
            SearchRequest searchRequest, Map<String, SearchField> searchFieldMap, Class<E> entityClass) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<E> root = criteriaQuery.from(entityClass);

        criteriaQuery.select(criteriaBuilder.count(root));

        addFilters(searchFieldMap, searchRequest, criteriaBuilder, criteriaQuery, root);

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        return query.getSingleResult().intValue();
    }

    private void validateSearchRequest(SearchRequest searchRequest, Map<String, SearchField> searchFieldMap) {
        int maxPageSize = searchEngineProperties.getMaxPageSize();
        if (searchRequest.size() > maxPageSize) {
            throw new HibernateSearchEngineException(
                    "The search request is limited to %s results".formatted(maxPageSize));
        }

        Optional.ofNullable(searchRequest.sorts())
                .orElseGet(Collections::emptyList)
                .forEach(sort -> validateExistingSearchField(searchFieldMap, sort.field()));
        Optional.ofNullable(searchRequest.filters())
                .orElseGet(Collections::emptyList)
                .forEach(filter -> validateExistingSearchField(searchFieldMap, filter.field()));
    }

    private void validateExistingSearchField(Map<String, SearchField> searchFieldMap, String field) {
        SearchField searchField = searchFieldMap.get(field);
        if (searchField == null) {
            Set<String> existingFields =
                    searchFieldMap.values().stream().map(SearchField::id).collect(Collectors.toSet());
            throw new HibernateSearchEngineException(
                    "Search field '%s' was not found! Existing fields: %s".formatted(field, existingFields));
        }
    }

    private void addFilters(
            Map<String, SearchField> searchFieldMap,
            SearchRequest searchRequest,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root) {
        Predicate predicate = criteriaBuilder.conjunction();

        var filters = searchRequest.filters();
        if (CollectionUtils.isEmpty(filters)) {
            return;
        }

        var searchConsumer = new FilterQueryCriteriaConsumer(criteriaBuilder, root, searchFieldMap, predicate);
        filters.forEach(searchConsumer);

        criteriaQuery.where(searchConsumer.getPredicate());
    }

    private void addSorts(
            Map<String, SearchField> searchFieldMap,
            SearchRequest searchRequest,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            Root<?> root) {
        var sorts = searchRequest.sorts();
        if (CollectionUtils.isEmpty(sorts)) {
            return;
        }

        List<Order> orders = sorts.stream()
                .map(sort -> {
                    String field = sort.field();
                    SearchField searchField = searchFieldMap.get(field);
                    return sort.order() == SortDirection.DESCENDING
                            ? criteriaBuilder.desc(getPath(root, searchField))
                            : criteriaBuilder.asc(getPath(root, searchField));
                })
                .toList();

        criteriaQuery.orderBy(orders);
    }

    private <Y> Path<Y> getPath(Root<?> root, SearchField searchField) {
        String[] fields = searchField.path().split("\\.");

        Path<Y> path = searchField.elementCollection() ? root.join(fields[0]) : root.get(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            path = path.get(fields[i]);
        }

        return path;
    }

    private class FilterQueryCriteriaConsumer implements Consumer<Filter> {

        private final CriteriaBuilder builder;
        private final Root<?> root;
        private final Map<String, SearchField> searchFields;
        private Predicate predicate;

        public FilterQueryCriteriaConsumer(
                CriteriaBuilder builder, Root<?> root, Map<String, SearchField> searchFields, Predicate predicate) {
            this.builder = builder;
            this.root = root;
            this.searchFields = searchFields;
            this.predicate = predicate;
        }

        public Predicate getPredicate() {
            return predicate;
        }

        @Override
        public void accept(Filter filter) {
            SearchField searchField = resolveSearchField(searchFields, filter);

            List<?> valueList = filter.value().stream()
                    .map(originalValue -> getConvertedValue(originalValue, searchField))
                    .toList();
            Object singleValue = valueList.getFirst();

            switch (filter.type()) {
                case IS_NULL -> predicate = builder.and(predicate, builder.isNull(getPath(searchField)));
                case IS_NOT_NULL -> predicate = builder.and(predicate, builder.isNotNull(getPath(searchField)));
                case EQUAL -> predicate = builder.and(predicate, builder.equal(getPath(searchField), singleValue));
                case NOT_EQUAL -> predicate =
                        builder.and(predicate, builder.notEqual(getPath(searchField), singleValue));
                case IN -> predicate =
                        builder.and(predicate, getPath(searchField).in(valueList));
                case NOT_IN -> predicate = builder.and(
                        predicate, getPath(searchField).in(valueList).not());
                case LIKE -> predicate = builder.and(
                        predicate,
                        builder.like(
                                builder.lower(getPath(searchField)),
                                "%" + singleValue.toString().toLowerCase() + "%"));
                case NOT_LIKE -> predicate = builder.and(
                        predicate,
                        builder.notLike(
                                builder.lower(getPath(searchField)),
                                "%" + singleValue.toString().toLowerCase() + "%"));
                case GREATER_THAN -> buildComparePredicate(GREATER_THAN, searchField, singleValue);
                case GREATER_THAN_OR_EQUAL -> buildComparePredicate(GREATER_THAN_OR_EQUAL, searchField, singleValue);
                case LESS_THAN -> buildComparePredicate(LESS_THAN, searchField, singleValue);
                case LESS_THAN_OR_EQUAL -> buildComparePredicate(LESS_THAN_OR_EQUAL, searchField, singleValue);
            }
        }

        private void buildComparePredicate(FilterType filterType, SearchField searchField, Object value) {
            if (value instanceof Integer castedValue) {
                buildComparePredicate(filterType, searchField, castedValue);
            }
            if (value instanceof Long castedValue) {
                buildComparePredicate(filterType, searchField, castedValue);
            }
            if (value instanceof BigDecimal castedValue) {
                buildComparePredicate(filterType, searchField, castedValue);
            }
            if (value instanceof Instant castedValue) {
                buildComparePredicate(filterType, searchField, castedValue);
            }
        }

        private <K extends Comparable<K>> void buildComparePredicate(
                FilterType filterType, SearchField searchField, K value) {
            switch (filterType) {
                case GREATER_THAN -> predicate =
                        builder.and(predicate, builder.greaterThan(getPath(searchField), value));
                case GREATER_THAN_OR_EQUAL -> predicate =
                        builder.and(predicate, builder.greaterThanOrEqualTo(getPath(searchField), value));
                case LESS_THAN -> predicate = builder.and(predicate, builder.lessThan(getPath(searchField), value));
                case LESS_THAN_OR_EQUAL -> predicate =
                        builder.and(predicate, builder.lessThanOrEqualTo(getPath(searchField), value));
                default -> throw new HibernateSearchEngineException(
                        String.format("Can't build compare predicate for filter type %s", filterType));
            }
        }

        private <Y> Path<Y> getPath(SearchField searchField) {
            return SearchService.this.getPath(root, searchField);
        }
    }
}
