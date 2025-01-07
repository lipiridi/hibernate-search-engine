package io.github.lipiridi.searchengine;

import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN;
import static io.github.lipiridi.searchengine.FilterType.GREATER_THAN_OR_EQUAL;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN;
import static io.github.lipiridi.searchengine.FilterType.LESS_THAN_OR_EQUAL;
import static io.github.lipiridi.searchengine.util.FieldConvertUtils.getConvertedValue;

import io.github.lipiridi.searchengine.config.SearchEngineProperties;
import io.github.lipiridi.searchengine.dto.Filter;
import io.github.lipiridi.searchengine.dto.SearchRequest;
import io.github.lipiridi.searchengine.dto.SearchResponse;
import io.github.lipiridi.searchengine.dto.Sort;
import io.github.lipiridi.searchengine.util.FieldConvertUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Transactional(readOnly = true)
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

    public <E> SearchResponse<E> search(
            SearchRequest searchRequest, Class<E> entityClass, Collection<SearchField> searchFields) {
        return search(searchRequest, entityClass, searchFields, null);
    }

    public <E> SearchResponse<E> search(
            SearchRequest searchRequest, Class<E> entityClass, Map<String, SearchField> searchFieldMap) {
        return search(searchRequest, entityClass, searchFieldMap, null);
    }

    public <E, M> SearchResponse<M> search(
            SearchRequest searchRequest, Class<E> entityClass, @Nullable Function<E, M> mapper) {
        var searchFields = searchFieldCreator.createFromClass(entityClass);
        return search(searchRequest, entityClass, searchFields, mapper);
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

    @SuppressWarnings("unchecked")
    private <E, M> SearchResponse<M> search(
            SearchRequest searchRequest,
            Class<E> entityClass,
            Map<String, SearchField> searchFieldMap,
            @Nullable Function<E, M> mapper) {
        validateSearchRequest(searchRequest, searchFieldMap);
        List<SearchFilterPair> searchFilterPairs = createSearchFilterPairs(searchRequest, searchFieldMap);
        List<SearchSortPair> searchSortPairs = createSearchSortPairs(searchRequest, searchFieldMap);
        boolean distinctNeeded = isDistinctNeeded(searchFilterPairs, searchSortPairs);

        List<E> entities =
                fetchEntities(searchRequest, entityClass, searchFilterPairs, searchSortPairs, distinctNeeded);
        long totalNumber = totalElements(entityClass, searchFilterPairs, distinctNeeded);

        List<M> mappedEntities = mapper == null
                ? (List<M>) entities
                : entities.stream().map(mapper).toList();

        return new SearchResponse<>(searchRequest, entities.size(), totalNumber, mappedEntities);
    }

    public <E> List<E> fetchEntities(SearchRequest searchRequest, Class<E> entityClass) {
        var searchFields = searchFieldCreator.createFromClass(entityClass);
        return fetchEntities(searchRequest, entityClass, searchFields);
    }

    public <E> List<E> fetchEntities(
            SearchRequest searchRequest, Class<E> entityClass, Collection<SearchField> searchFields) {
        Map<String, SearchField> searchFieldMap =
                searchFields.stream().collect(Collectors.toMap(SearchField::id, Function.identity()));

        validateSearchRequest(searchRequest, searchFieldMap);
        List<SearchFilterPair> searchFilterPairs = createSearchFilterPairs(searchRequest, searchFieldMap);
        List<SearchSortPair> searchSortPairs = createSearchSortPairs(searchRequest, searchFieldMap);
        boolean distinctNeeded = isDistinctNeeded(searchFilterPairs, searchSortPairs);

        return fetchEntities(searchRequest, entityClass, searchFilterPairs, searchSortPairs, distinctNeeded);
    }

    private <E> List<E> fetchEntities(
            SearchRequest searchRequest,
            Class<E> entityClass,
            List<SearchFilterPair> searchFilterPairs,
            List<SearchSortPair> searchSortPairs,
            boolean distinctNeeded) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> root = criteriaQuery.from(entityClass);
        criteriaQuery.distinct(distinctNeeded);

        addFilters(root, criteriaBuilder, criteriaQuery, searchFilterPairs);
        addSorts(root, criteriaBuilder, criteriaQuery, searchSortPairs);

        TypedQuery<E> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((searchRequest.page() - 1) * searchRequest.size());
        query.setMaxResults(searchRequest.size());

        return query.getResultList();
    }

    public <E> long totalElements(SearchRequest searchRequest, Class<E> entityClass) {
        var searchFields = searchFieldCreator.createFromClass(entityClass);
        return totalElements(searchRequest, entityClass, searchFields);
    }

    public <E> long totalElements(
            SearchRequest searchRequest, Class<E> entityClass, Collection<SearchField> searchFields) {
        Map<String, SearchField> searchFieldMap =
                searchFields.stream().collect(Collectors.toMap(SearchField::id, Function.identity()));

        validateSearchRequest(searchRequest, searchFieldMap);
        List<SearchFilterPair> searchFilterPairs = createSearchFilterPairs(searchRequest, searchFieldMap);
        boolean distinctNeeded = isDistinctNeeded(searchFilterPairs, Collections.emptyList());

        return totalElements(entityClass, searchFilterPairs, distinctNeeded);
    }

    private <E> long totalElements(
            Class<E> entityClass, List<SearchFilterPair> searchFilterPairs, boolean distinctNeeded) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<E> root = criteriaQuery.from(entityClass);

        Expression<Long> countExpression =
                distinctNeeded ? criteriaBuilder.countDistinct(root) : criteriaBuilder.count(root);
        criteriaQuery.select(countExpression);

        addFilters(root, criteriaBuilder, criteriaQuery, searchFilterPairs);

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        return query.getSingleResult();
    }

    public Map<Class<?>, List<SearchField>> getCollectedSearchFields() {
        return searchFieldCreator.getCollectedSearchFields();
    }

    public @Nullable List<SearchField> getCollectedSearchFields(Class<?> entityClass) {
        return getCollectedSearchFields().get(entityClass);
    }

    @Nonnull
    private List<SearchFilterPair> createSearchFilterPairs(
            SearchRequest searchRequest, Map<String, SearchField> searchFieldMap) {
        var filters = searchRequest.filters();
        if (CollectionUtils.isEmpty(filters)) {
            return Collections.emptyList();
        }

        return filters.stream()
                .map(filter ->
                        new SearchFilterPair(filter, FieldConvertUtils.resolveSearchField(searchFieldMap, filter)))
                .toList();
    }

    @Nonnull
    private List<SearchSortPair> createSearchSortPairs(
            SearchRequest searchRequest, Map<String, SearchField> searchFieldMap) {
        var sorts = searchRequest.sorts();
        if (CollectionUtils.isEmpty(sorts)) {
            return Collections.emptyList();
        }

        return sorts.stream()
                .map(sort -> new SearchSortPair(sort, FieldConvertUtils.resolveSearchField(searchFieldMap, sort)))
                .toList();
    }

    private boolean isDistinctNeeded(
            @Nonnull List<SearchFilterPair> searchFilterPairs, @Nonnull List<SearchSortPair> searchSortPairs) {
        return searchFilterPairs.stream().map(SearchFilterPair::searchField).anyMatch(SearchField::distinct)
                || searchSortPairs.stream().map(SearchSortPair::searchField).anyMatch(SearchField::distinct);
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
            Root<?> root,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            List<SearchFilterPair> searchFilterPairs) {
        Predicate predicate = criteriaBuilder.conjunction();
        if (CollectionUtils.isEmpty(searchFilterPairs)) {
            return;
        }

        var searchConsumer = new FilterQueryCriteriaConsumer(criteriaBuilder, root, predicate);
        searchFilterPairs.forEach(searchConsumer);

        criteriaQuery.where(searchConsumer.getPredicate());
    }

    private void addSorts(
            Root<?> root,
            CriteriaBuilder criteriaBuilder,
            CriteriaQuery<?> criteriaQuery,
            List<SearchSortPair> searchSortPairs) {
        if (CollectionUtils.isEmpty(searchSortPairs)) {
            return;
        }

        List<Order> orders = searchSortPairs.stream()
                .map(searchSortPair -> {
                    SearchField searchField = searchSortPair.searchField();
                    Path<?> path = getPath(root, searchField);

                    return searchSortPair.sort().direction() == SortDirection.DESCENDING
                            ? criteriaBuilder.desc(path)
                            : criteriaBuilder.asc(path);
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

    private class FilterQueryCriteriaConsumer implements Consumer<SearchFilterPair> {

        private final CriteriaBuilder builder;
        private final Root<?> root;
        private Predicate predicate;

        public FilterQueryCriteriaConsumer(CriteriaBuilder builder, Root<?> root, Predicate predicate) {
            this.builder = builder;
            this.root = root;
            this.predicate = predicate;
        }

        public Predicate getPredicate() {
            return predicate;
        }

        @Override
        public void accept(SearchFilterPair searchFilterPair) {
            Filter filter = searchFilterPair.filter();
            SearchField searchField = searchFilterPair.searchField();

            FilterType filterType = filter.type();

            List<?> valueList = filterType.isNullAllowed()
                    ? null
                    : filter.value().stream()
                            .map(originalValue -> getConvertedValue(originalValue, searchField))
                            .toList();
            Object singleValue = filterType.isNullAllowed() ? null : valueList.getFirst();

            switch (filterType) {
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
                        predicate, builder.like(builder.lower(getPath(searchField)), getLikeValue(singleValue)));
                case NOT_LIKE -> predicate = builder.and(
                        predicate, builder.notLike(builder.lower(getPath(searchField)), getLikeValue(singleValue)));
                case GREATER_THAN -> buildComparePredicate(GREATER_THAN, searchField, singleValue);
                case GREATER_THAN_OR_EQUAL -> buildComparePredicate(GREATER_THAN_OR_EQUAL, searchField, singleValue);
                case LESS_THAN -> buildComparePredicate(LESS_THAN, searchField, singleValue);
                case LESS_THAN_OR_EQUAL -> buildComparePredicate(LESS_THAN_OR_EQUAL, searchField, singleValue);
            }
        }

        private String getLikeValue(@Nullable Object singleValue) {
            return singleValue == null ? "" : "%" + singleValue.toString().toLowerCase() + "%";
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

    private record SearchFilterPair(Filter filter, SearchField searchField) {}

    private record SearchSortPair(Sort sort, SearchField searchField) {}
}
