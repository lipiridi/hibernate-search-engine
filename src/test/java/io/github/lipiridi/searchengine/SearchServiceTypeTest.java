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
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SearchServiceTypeTest {

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
    void search_UUIDFilter_ReturnsValid() {
        Object filterValue = UUID.nameUUIDFromBytes(("simple-" + 1).getBytes(StandardCharsets.UTF_8));
        assertReturnedRows(SimpleEntity.Fields.id, filterValue, 1, 1);
    }

    @Test
    void search_StringFilter_ReturnsValid() {
        Object filterValue = "Name " + 1;
        assertReturnedRows(SimpleEntity.Fields.stringValue, filterValue, 1, 1);
    }

    @Test
    void search_BooleanFilter_ReturnsValid() {
        Object filterValue = true;
        assertReturnedRows(SimpleEntity.Fields.booleanValue, filterValue, 5, 25);
    }

    @Test
    void search_ByteFilter_ReturnsValid() {
        Object filterValue = (byte) 1; // byteValue = i
        assertReturnedRows(SimpleEntity.Fields.byteValue, filterValue, 1, 1);
    }

    @Test
    void search_ShortFilter_ReturnsValid() {
        Object filterValue = (short) 1; // shortValue = i
        assertReturnedRows(SimpleEntity.Fields.shortValue, filterValue, 1, 1);
    }

    @Test
    void search_IntegerFilter_ReturnsValid() {
        Object filterValue = 1; // intValue = i
        assertReturnedRows(SimpleEntity.Fields.intValue, filterValue, 1, 1);
    }

    @Test
    void search_LongFilter_ReturnsValid() {
        Object filterValue = 1L; // quantity = i
        assertReturnedRows(SimpleEntity.Fields.longValue, filterValue, 1, 1);
    }

    @Test
    void search_DoubleFilter_ReturnsValid() {
        Object filterValue = 2.2d; // doubleValue = i * 2.2 for i=1
        assertReturnedRows(SimpleEntity.Fields.doubleValue, filterValue, 1, 1);
    }

    @Test
    void search_FloatFilter_ReturnsValid() {
        Object filterValue = 1.1f; // weight = i * 1.1 for i=1
        assertReturnedRows(SimpleEntity.Fields.floatValue, filterValue, 1, 1);
    }

    @Test
    void search_BigDecimalFilter_ReturnsValid() {
        Object filterValue = BigDecimal.valueOf(10L); // price = i*10 for i=1
        assertReturnedRows(SimpleEntity.Fields.bigDecimalValue, filterValue, 1, 1);
    }

    @Test
    void search_InstantFilter_ReturnsValid() {
        Instant baseTime = Instant.parse("2020-01-01T00:00:00Z");
        Instant filterValue = baseTime.plusSeconds(1); // for i=1
        assertReturnedRows(SimpleEntity.Fields.instantValue, filterValue, 1, 1);
    }

    @Test
    void search_LocalDateFilter_ReturnsValid() {
        LocalDate baseDate = LocalDate.parse("2020-01-01");
        LocalDate filterValue = baseDate.plusDays(1); // for i=1
        assertReturnedRows(SimpleEntity.Fields.localDateValue, filterValue, 1, 1);
    }

    @Test
    void search_LocalDateTimeFilter_ReturnsValid() {
        LocalDateTime base = LocalDateTime.parse("2020-01-01T00:00:00");
        LocalDateTime filterValue = base.plusSeconds(1);
        assertReturnedRows(SimpleEntity.Fields.localDateTimeValue, filterValue, 1, 1);
    }

    @Test
    void search_ZonedDateTimeFilter_ReturnsValid() {
        ZonedDateTime baseTime = ZonedDateTime.parse("2020-01-01T01:00:00+01:00");
        ZonedDateTime filterValue = baseTime.plusSeconds(1);
        assertReturnedRows(SimpleEntity.Fields.zonedDateTimeValue, filterValue, 1, 1);
    }

    @Test
    void search_OffsetDateTimeFilter_ReturnsValid() {
        OffsetDateTime base = OffsetDateTime.parse("2020-01-01T01:00:00+01:00");
        OffsetDateTime filterValue = base.plusSeconds(1);
        assertReturnedRows(SimpleEntity.Fields.offsetDateTimeValue, filterValue, 1, 1);
    }

    @Test
    void search_CurrencyFilter_ReturnsValid() {
        Object filterValue = Currency.getInstance("USD"); // even i
        assertReturnedRows(SimpleEntity.Fields.currency, filterValue, 5, 25);
    }

    @Test
    void search_EnumOrdinalFilter_ReturnsValid() {
        Object filterValue = Operation.ADD.ordinal();
        assertReturnedRows(SimpleEntity.Fields.operation, filterValue, 5, 25);
    }

    @Test
    void search_EnumStringFilter_ReturnsValid() {
        Object filterValue = Operation.ADD.name();
        assertReturnedRows(SimpleEntity.Fields.operationAsString, filterValue, 5, 25);
    }

    private void assertReturnedRows(String name, Object filterValue, int elementsExpected, int totalElementsExpected) {
        Filter filter = new Filter(name, FilterType.EQUAL, filterValue.toString());
        SearchRequest searchRequest = new SearchRequest(1, 5, filter);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertEquals(elementsExpected, response.elements());
        Assertions.assertEquals(totalElementsExpected, response.totalElements());
    }
}
