package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.dto.Filter;
import io.github.lipiridi.searchengine.dto.SearchRequest;
import io.github.lipiridi.searchengine.dto.SearchResponse;
import io.github.lipiridi.searchengine.entity.Operation;
import io.github.lipiridi.searchengine.entity.SimpleEntity;
import io.github.lipiridi.searchengine.repository.SimpleEntityRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchServiceFilterTest {

    @Autowired
    SearchService searchService;

    @BeforeAll
    static void beforeAll(@Autowired SimpleEntityRepository simpleEntityRepository) {

        List<SimpleEntity> entities = new ArrayList<>(50);
        Instant baseInstant = Instant.parse("2020-01-01T00:00:00Z");
        LocalDate baseDate = LocalDate.parse("2020-01-01");
        LocalDateTime baseLocalDateTime = LocalDateTime.parse("2020-01-01T00:00:00");
        OffsetDateTime baseOffsetDateTime = OffsetDateTime.parse("2020-01-01T00:00:00Z");

        for (int i = 1; i <= 50; i++) {
            SimpleEntity e = new SimpleEntity();

            UUID id = UUID.nameUUIDFromBytes(("simple-" + i).getBytes(StandardCharsets.UTF_8));
            e.setId(id);

            e.setStringValue("Name " + i);
            e.setBooleanValue(i % 2 == 0);
            e.setByteValue((byte) i);
            e.setShortValue((short) i);
            e.setIntValue(i);
            e.setLongValue((long) i);
            e.setDoubleValue(i * 2.2d);
            e.setFloatValue((float) (i * 1.1));
            e.setBigDecimalValue(BigDecimal.valueOf(i * 10L));

            Instant created = baseInstant.plusSeconds(i);
            e.setInstantValue(created);
            e.setLocalDateValue(baseDate.plusDays(i));
            e.setLocalDateTimeValue(baseLocalDateTime.plusSeconds(i));
            e.setZonedDateTimeValue(ZonedDateTime.ofInstant(created, ZoneOffset.UTC));
            e.setOffsetDateTimeValue(baseOffsetDateTime.plusSeconds(i));

            e.setCurrency(i % 2 == 0 ? Currency.getInstance("USD") : Currency.getInstance("EUR"));

            Operation op = i % 2 == 0 ? Operation.ADD : Operation.SUBTRACT;
            e.setOperation(op);
            e.setOperationAsString(op);

            entities.add(e);
        }

        simpleEntityRepository.saveAll(entities);
    }

    @Test
    void search_IsNullFilter_ReturnsValid() {
        assertReturnedRows(new Filter(SimpleEntity.Fields.nullStringValue, FilterType.IS_NULL, (String) null), 5, 50);
    }

    @Test
    void search_IsNotNullFilter_ReturnsValid() {
        assertReturnedRows(
                new Filter(SimpleEntity.Fields.nullStringValue, FilterType.IS_NOT_NULL, (String) null), 0, 0);
    }

    @Test
    void search_EqualFilter_ReturnsValid() {
        String filterValue = UUID.nameUUIDFromBytes(("simple-" + 1).getBytes(StandardCharsets.UTF_8))
                .toString();
        assertReturnedRows(new Filter(SimpleEntity.Fields.id, FilterType.EQUAL, filterValue), 1, 1);
    }

    @Test
    void search_NotEqualFilter_ReturnsValid() {
        String filterValue = UUID.nameUUIDFromBytes(("simple-" + 1).getBytes(StandardCharsets.UTF_8))
                .toString();
        assertReturnedRows(new Filter(SimpleEntity.Fields.id, FilterType.NOT_EQUAL, filterValue), 5, 49);
    }

    @Test
    void search_InFilter_ReturnsValid() {
        String filterValue1 = UUID.nameUUIDFromBytes(("simple-" + 1).getBytes(StandardCharsets.UTF_8))
                .toString();
        String filterValue2 = UUID.nameUUIDFromBytes(("simple-" + 2).getBytes(StandardCharsets.UTF_8))
                .toString();
        assertReturnedRows(new Filter(SimpleEntity.Fields.id, FilterType.IN, Set.of(filterValue1, filterValue2)), 2, 2);
    }

    @Test
    void search_NotInFilter_ReturnsValid() {
        String filterValue1 = UUID.nameUUIDFromBytes(("simple-" + 1).getBytes(StandardCharsets.UTF_8))
                .toString();
        String filterValue2 = UUID.nameUUIDFromBytes(("simple-" + 2).getBytes(StandardCharsets.UTF_8))
                .toString();
        assertReturnedRows(
                new Filter(SimpleEntity.Fields.id, FilterType.NOT_IN, Set.of(filterValue1, filterValue2)), 5, 48);
    }

    @Test
    void search_LikeFilter_ReturnsValid() {
        String filterValue = "aMe"; // like filter is case-insensitive
        assertReturnedRows(new Filter(SimpleEntity.Fields.stringValue, FilterType.LIKE, filterValue), 5, 50);
    }

    @Test
    void search_NotLikeFilter_ReturnsValid() {
        String filterValue = "aMe"; // like filter is case-insensitive
        assertReturnedRows(new Filter(SimpleEntity.Fields.stringValue, FilterType.NOT_LIKE, filterValue), 0, 0);
    }

    @Test
    void search_GreaterThanFilter_ReturnsValid() {
        String filterValue = Integer.toString(25);
        assertReturnedRows(new Filter(SimpleEntity.Fields.intValue, FilterType.GREATER_THAN, filterValue), 5, 25);
    }

    @Test
    void search_LessThanFilter_ReturnsValid() {
        String filterValue = Integer.toString(25);
        assertReturnedRows(new Filter(SimpleEntity.Fields.intValue, FilterType.LESS_THAN, filterValue), 5, 24);
    }

    @Test
    void search_GreaterThanOrEqualFilter_ReturnsValid() {
        String filterValue = Integer.toString(25);
        assertReturnedRows(
                new Filter(SimpleEntity.Fields.intValue, FilterType.GREATER_THAN_OR_EQUAL, filterValue), 5, 26);
    }

    @Test
    void search_LessThanOrEqualFilter_ReturnsValid() {
        String filterValue = Integer.toString(25);
        assertReturnedRows(new Filter(SimpleEntity.Fields.intValue, FilterType.LESS_THAN_OR_EQUAL, filterValue), 5, 25);
    }

    private void assertReturnedRows(Filter filter, int elementsExpected, int totalElementsExpected) {
        SearchRequest searchRequest = new SearchRequest(1, 5, filter);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertEquals(elementsExpected, response.elements());
        Assertions.assertEquals(totalElementsExpected, response.totalElements());
    }
}
