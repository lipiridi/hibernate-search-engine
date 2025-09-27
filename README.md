# Hibernate Search Engine

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lipiridi/hibernate-search-engine)](https://search.maven.org/artifact/io.github.lipiridi/hibernate-search-engine)

The Hibernate Search Engine is a powerful Java library designed to simplify the implementation of search functionality,
filtering, sorting, and pagination in Java projects using the Hibernate framework. With minimal configuration,
developers can seamlessly integrate search capabilities into their applications, enhancing the user experience and
improving data retrieval efficiency.

## Features

- **Annotation-based Searchable Entities:** Simply annotate the fields of your entity classes with `@Searchable` to
  enable search functionality.


- **Wide support:** Use `@Searchable` with primitive types and various relationships, including `@OneToOne`,
  `@OneToMany`, `@ManyToOne`,
  `@ManyToMany`, `@ElementCollection`. Use nested fields from these relationships in search requests.


- **SearchService Integration:** Inject the SearchService into your code, and effortlessly perform searches by
  calling `searchService.search(searchRequest, Entity.class)`


- **Mapping Support:** The library supports mapping search results to another class, such as a Data Transfer Object
  (DTO), directly within the service. This enables efficient transformation of data for various use cases.

## Getting Started

### Installation

To include this library in your project, add the following dependency:

Gradle:

```kotlin
implementation("io.github.lipiridi:hibernate-search-engine:1.3.0")
```

Maven:

```xml

<dependency>
    <groupId>io.github.lipiridi</groupId>
    <artifactId>hibernate-search-engine</artifactId>
    <version>1.3.0</version> <!-- Replace with the latest version -->
</dependency>
```

### Configuration

Customize the library's behavior with the help of configuration properties.

- **Max page size** - limit the search request in order to prohibit large queries to the database
- **Naming convention** - choose how to generate field names that uses client for searching (in case when you use
  @Searchable annotation)

```properties
spring.jpa.hibernate.search-engine.max-page-size=100
spring.jpa.hibernate.search-engine.naming-convention=camel_case
```

## Usage example

```java

@Entity
public class TestEntity {

    @Searchable
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Searchable
    String description;

    @Searchable
    @ManyToOne
    @JoinColumn
    Image image;

    @Searchable
    int sortOrder;

    @Searchable
    @CreationTimestamp
    Instant createdAt;

    @Searchable
    boolean enabled;

    @Searchable
    @OneToMany
    Set<TestAttribute> attributes = new HashSet<>();

    @Searchable
    @ElementCollection
    List<String> labels = new ArrayList<>();
}
```

```java

@RestController
@RequiredArgsConstructor
public class TestController {

    private final SearchService searchService;
    private final TestMapper testMapper;

    @PostMapping("/search")
    public SearchResponse<TestDto> search(@Valid @RequestBody SearchRequest searchRequest) {
        return searchService.search(searchRequest, TestEntity.class, testMapper::toDto);
    }
}
```

## Sample search request

Here's an example of the search request JSON body output:

```json
{
  "page": 1,
  "size": 100,
  "filters": [
    {
      "field": "description",
      "type": "LIKE",
      "value": [
        "hello"
      ]
    },
    {
      "field": "id",
      "type": "IN",
      "value": [
        "16",
        "25"
      ]
    }
  ],
  "sorts": [
    {
      "field": "createdAt",
      "direction": "ASCENDING"
    },
    {
      "field": "imageName",
      "direction": "DESCENDING"
    }
  ]
}
```

## Filters and supported types

The library supports a rich set of filters. Each filter is available only for specific Java types. If a filter is used
with an unsupported field type, the library throws an error with a list of available filters for that field.

Supported filters:

- `IS_NULL`, `IS_NOT_NULL`: checks whether the field is null or not. Value is not required.
- `EQUAL`, `NOT_EQUAL`: exact equality/inequality comparison.
- `IN`, `NOT_IN`: field value is contained/not contained in the provided list. Provide an array of values in the request.
- `LIKE`, `NOT_LIKE`: case-insensitive substring match for String fields only.
- `GREATER_THAN`, `LESS_THAN`, `GREATER_THAN_OR_EQUAL`, `LESS_THAN_OR_EQUAL`: comparison filters for comparable types.

Type groups used by filters:

- Common types (for `IS_NULL`/`IS_NOT_NULL`, `EQUAL`/`NOT_EQUAL`, `IN`/`NOT_IN`):
    - String, Boolean, UUID, Currency, Enum
    - Plus all Comparable types listed below
- Comparable types (for  GREATER/LESS variants in addition to common filters):
    - Byte, Short, Integer, Long, Double, Float, BigDecimal
    - Instant, LocalDate, LocalDateTime, ZonedDateTime, OffsetDateTime
- `LIKE`/`NOT_LIKE`: String only

> [!NOTE]
> - Null handling: Only `IS_NULL` and `IS_NOT_NULL` allow a null value. All other filters require a non-null value.
> - `IN`/`NOT_IN`: supply value as an array. If the array is empty, no matches will be found.
> - When a field is annotated with @Searchable and additionally restricts filterTypes, only that subset is allowed.

## Value conversion rules

The library converts incoming JSON string values to target Java types automatically:

- Numbers: Byte, Short, Integer, Long, Float, Double, BigDecimal are parsed from their string representations.
- Booleans: "true"/"false" (case-insensitive) to Boolean.
- UUID: standard UUID format.
- Currency: ISO 4217 currency code (e.g. "USD", "eur").
- Enums: by name (case-insensitive; value is uppercased) or by ordinal if a numeric value is provided.
- Temporal types:
    - ISO-8601 strings are supported for Instant, LocalDate, LocalDateTime, ZonedDateTime, OffsetDateTime.
    - Epoch timestamps are also supported for time types:
        - Pure integer is interpreted as epoch milliseconds.
        - Fractional numbers are supported and interpreted smartly as seconds with fractional part (or milliseconds if
          the integer part is very large). The library converts them to the requested time type using UTC.

If conversion fails, the request is rejected with a clear error message indicating the field and value.

## Total elements endpoint (count only)

When you need only the total number of elements that match filters (without pagination and sorting), use
TotalElementsRequest/TotalElementsResponse.

Java API:

- searchService.totalElements(TotalElementsRequest request, Class<E> entityClass)
- Returns TotalElementsResponse with a single field: totalElements

Example request:

```json
{
  "filters": [
    {
      "field": "status",
      "type": "EQUAL",
      "value": [
        "ACTIVE"
      ]
    },
    {
      "field": "createdAt",
      "type": "GREATER_THAN_OR_EQUAL",
      "value": [
        "2024-01-01T00:00:00Z"
      ]
    }
  ]
}
```

Example response:

```json
{
  "totalElements": 42
}
```

Notes:

- The same validation rules and type conversions as for SearchRequest apply here.
- Distinct is applied automatically when filters require joins over collections to avoid overcounting.

## Additional validations and behavior

- Invalid filter type: If a filter is not allowed for a field, an error is thrown with available options for that field.
- Missing value: For filters other than `IS_NULL`/`IS_NOT_NULL`, a null value triggers a validation error.
- Sorting limitations: Sorting by fields that require distinct over joined collections is prohibited and will be
  rejected.
- Field names: The effective field id depends on the configured naming convention and can include nested properties from
  relationships annotated with @Searchable.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
