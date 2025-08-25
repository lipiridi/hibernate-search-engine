package io.github.lipiridi.searchengine;

import io.github.lipiridi.searchengine.dto.SearchRequest;
import io.github.lipiridi.searchengine.dto.SearchResponse;
import io.github.lipiridi.searchengine.entity.Operation;
import io.github.lipiridi.searchengine.entity.SimpleEntity;
import io.github.lipiridi.searchengine.repository.SimpleEntityRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@SpringBootTest(properties = "spring.jpa.show-sql=true")
@ExtendWith(OutputCaptureExtension.class)
class SearchServiceCommonTest {

    @Autowired
    SearchService searchService;

    @BeforeAll
    static void beforeAll(@Autowired SimpleEntityRepository simpleEntityRepository) {

        // Ensure a clean state
        // simpleEntityRepository.deleteAllInBatch();

        List<SimpleEntity> entities = new ArrayList<>(50);
        Instant baseInstant = Instant.parse("2020-01-01T00:00:00Z");
        ZoneId utc = ZoneId.of("UTC");

        for (int i = 1; i <= 50; i++) {
            SimpleEntity e = new SimpleEntity();

            UUID id = UUID.nameUUIDFromBytes(("simple-" + i).getBytes(StandardCharsets.UTF_8));
            e.setId(id);

            e.setStringValue("Name " + i);
            e.setBooleanValue(i % 2 == 0);
            e.setLongValue((long) i);
            e.setFloatValue((float) (i * 1.1));
            e.setBigDecimalValue(BigDecimal.valueOf(i * 10L));

            Instant created = baseInstant.plusSeconds(i);
            e.setInstantValue(created);
            e.setZonedDateTimeValue(ZonedDateTime.ofInstant(created, utc));

            e.setCurrency(i % 2 == 0 ? Currency.getInstance("USD") : Currency.getInstance("EUR"));

            Operation op = i % 2 == 0 ? Operation.ADD : Operation.SUBTRACT;
            e.setOperation(op);
            e.setOperationAsString(op);

            entities.add(e);
        }

        simpleEntityRepository.saveAll(entities);
    }

    @Test
    void search_WithoutFilters_ReturnsAllEntities() {
        SearchRequest searchRequest = new SearchRequest(1, 100);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertEquals(50, response.elements());
        Assertions.assertEquals(50, response.totalElements());
    }

    @Test
    void search_RequestedLessThanExists_ReturnsRequestedSize() {
        SearchRequest searchRequest = new SearchRequest(1, 20);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertEquals(20, response.elements());
        Assertions.assertEquals(50, response.totalElements());
    }

    @Test
    void search_WithoutTotal_ReturnsTotalNull() {
        SearchRequest searchRequest = new SearchRequest(1, 20, null, null, true);
        SearchResponse<SimpleEntity> response = searchService.search(searchRequest, SimpleEntity.class);
        Assertions.assertNull(response.totalElements());
    }

    @Test
    void search_StandardSearchRequest_SelectAndCountQueries(CapturedOutput output) {
        assertCapturedQueryLog(output, false);
    }

    @Test
    void search_SearchRequestWithoutTotal_SelectOnlyQuery(CapturedOutput output) {
        assertCapturedQueryLog(output, true);
    }

    private void assertCapturedQueryLog(CapturedOutput output, boolean withoutTotals) {
        // Capture baseline output before executing the search to exclude startup DDL/DML logs
        String baseline = (output.getOut() + "\n" + output.getErr());

        // When: perform a paged search that should trigger a select and a count query
        SearchRequest searchRequest = new SearchRequest(1, 1, null, null, withoutTotals);
        searchService.search(searchRequest, SimpleEntity.class);

        // Then: capture console output
        String after = (output.getOut() + "\n" + output.getErr());

        // Analyze only the newly appended part to avoid interference from other logs
        String deltaLog = after.length() >= baseline.length() ? after.substring(baseline.length()) : after;

        int countSelectCount = countMatches(
                deltaLog, Pattern.compile("\\bselect\\s+count\\b", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        int countSimpleSelect = countMatches(
                deltaLog, Pattern.compile("\\bselect\\b(?!\\s+count)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));

        int expectedSelectCount = withoutTotals ? 0 : 1;
        // Expect exactly one simple select and exactly one select count
        if (countSimpleSelect != 1 || countSelectCount != expectedSelectCount) {
            String debugTail = deltaLog.length() > 4000 ? deltaLog.substring(deltaLog.length() - 4000) : deltaLog;
            Assertions.fail(
                    "Expected exactly 1 simple SELECT and %d SELECT COUNT during search, but found: simpleSelect=%d, selectCount=%d\nAppended logs:\n%s"
                            .formatted(expectedSelectCount, countSimpleSelect, countSelectCount, debugTail));
        }
    }

    private static int countMatches(String log, Pattern pattern) {
        Matcher matcher = pattern.matcher(log);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
