package io.github.lipiridi.searchengine;

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
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.jpa.show-sql=true")
class SearchServiceJoinTest {

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

            e.setName("Name " + i);
            e.setActive(i % 2 == 0);
            e.setQuantity((long) i);
            e.setWeight((float) (i * 1.1));
            e.setPrice(BigDecimal.valueOf(i * 10L));

            Instant created = baseInstant.plusSeconds(i);
            e.setCreatedAt(created);
            e.setCommentedAt(ZonedDateTime.ofInstant(created, utc));

            e.setCurrency(i % 2 == 0 ? Currency.getInstance("USD") : Currency.getInstance("EUR"));

            Operation op = i % 2 == 0 ? Operation.ADD : Operation.SUBTRACT;
            e.setOperation(op);
            e.setOperationAsString(op);

            entities.add(e);
        }

        simpleEntityRepository.saveAll(entities);
    }
}
