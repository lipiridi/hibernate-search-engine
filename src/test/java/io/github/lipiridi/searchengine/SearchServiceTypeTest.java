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

        // Ensure a clean state
        // simpleEntityRepository.deleteAllInBatch();

        List<SimpleEntity> entities = new ArrayList<>(50);
        Instant baseInstant = Instant.parse("2020-01-01T00:00:00Z");

        for (int i = 1; i <= 50; i++) {
            SimpleEntity e = new SimpleEntity();

            UUID id = UUID.nameUUIDFromBytes(("simple-" + i).getBytes(StandardCharsets.UTF_8));
            e.setId(id);

            e.setName("Name " + i);
            e.setActive(i % 2 == 0);
            e.setQuantity((long) i);
            e.setWeight((float) (i * 1.1));
            e.setPrice(BigDecimal.valueOf(i * 10L));

            Instant created = baseInstant.plusSeconds(i);
            e.setCreatedAt(created);
            e.setCommentedAt(ZonedDateTime.ofInstant(created, ZoneOffset.UTC));

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
        assertReturnedRows("id", filterValue, 1, 1);
    }

    @Test
    void search_StringFilter_ReturnsValid() {
        Object filterValue = "Name " + 1;
        assertReturnedRows("name", filterValue, 1, 1);
    }

    @Test
    void search_BooleanFilter_ReturnsValid() {
        Object filterValue = true;
        assertReturnedRows("active", filterValue, 5, 25);
    }

    @Test
    void search_LongFilter_ReturnsValid() {
        Object filterValue = 1L; // quantity = i
        assertReturnedRows("quantity", filterValue, 1, 1);
    }

    @Test
    void search_FloatFilter_ReturnsValid() {
        Object filterValue = 1.1f; // weight = i * 1.1 for i=1
        assertReturnedRows("weight", filterValue, 1, 1);
    }

    @Test
    void search_BigDecimalFilter_ReturnsValid() {
        Object filterValue = BigDecimal.valueOf(10L); // price = i*10 for i=1
        assertReturnedRows("price", filterValue, 1, 1);
    }

    @Test
    void search_InstantFilter_ReturnsValid() {
        Instant baseTime = Instant.parse("2020-01-01T00:00:00Z");
        Instant filterValue = baseTime.plusSeconds(1); // for i=1
        assertReturnedRows("createdAt", filterValue, 1, 1);
    }

    @Test
    void search_ZonedDateTimeFilter_ReturnsValid() {
        ZonedDateTime baseTime = ZonedDateTime.parse("2020-01-01T01:00:00+01:00");
        ZonedDateTime filterValue = baseTime.plusSeconds(1);
        assertReturnedRows("commentedAt", filterValue, 1, 1);
    }

    @Test
    void search_CurrencyFilter_ReturnsValid() {
        Object filterValue = Currency.getInstance("USD"); // even i
        assertReturnedRows("currency", filterValue, 5, 25);
    }

    @Test
    void search_EnumFilter_ReturnsValid() {
        Object filterValue = Operation.ADD; // even i
        assertReturnedRows("operation", filterValue, 5, 25);
    }

    private void assertReturnedRows(String name, Object filterValue, int elementsExpected, int totalElementsExpected) {
        Filter filter = new Filter(name, FilterType.EQUAL, filterValue.toString());
        SearchRequest searchRequest = new SearchRequest(1, 5, filter);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertEquals(elementsExpected, response.elements());
        Assertions.assertEquals(totalElementsExpected, response.totalElements());
    }
}
