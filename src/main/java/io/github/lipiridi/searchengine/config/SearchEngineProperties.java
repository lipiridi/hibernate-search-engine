package io.github.lipiridi.searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.jpa.hibernate.search-engine")
public class SearchEngineProperties {

    int maxPageSize = 100;
    NameConvention nameConvention = NameConvention.CAMEL_CASE;

    public enum NameConvention {
        CAMEL_CASE,
        SNAKE_CASE,
        DOT_CASE
    }
}
