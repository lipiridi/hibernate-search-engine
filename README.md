# Hibernate Search Engine

[![Maven Central](https://img.shields.io/maven-central/v/io.github.lipiridi/hibernate-search-engine)](https://search.maven.org/artifact/io.github.lipiridi/hibernate-search-engine)

The Hibernate Search Engine is a powerful Java library designed to simplify the implementation of search functionality,
filtering, sorting, and pagination in Java projects using the Hibernate framework. With minimal configuration,
developers can seamlessly integrate search capabilities into their applications, enhancing the user experience and
improving data retrieval efficiency.

## Features

- **Annotation-based Searchable Entities:** Simply annotate the fields of your entity classes with `@Searchable` to
  enable
  search functionality.


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
implementation("io.github.lipiridi:hibernate-search-engine:1.1.1")
```

Maven:

```xml

<dependency>
    <groupId>io.github.lipiridi</groupId>
    <artifactId>hibernate-search-engine</artifactId>
    <version>1.1.1</version> <!-- Replace with the latest version -->
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
  "page": "1",
  "size": "100",
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

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
