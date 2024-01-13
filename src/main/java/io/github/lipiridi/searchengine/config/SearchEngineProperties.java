package io.github.lipiridi.searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.jpa.hibernate.search-engine")
public class SearchEngineProperties {

    int maxPageSize = 100;
}
