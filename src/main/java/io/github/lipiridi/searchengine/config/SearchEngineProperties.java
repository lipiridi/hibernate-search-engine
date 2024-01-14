package io.github.lipiridi.searchengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jpa.hibernate.search-engine")
public class SearchEngineProperties {

    int maxPageSize = 100;

    public int getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public NamingConvention getNamingConvention() {
        return namingConvention;
    }

    public void setNamingConvention(NamingConvention namingConvention) {
        this.namingConvention = namingConvention;
    }

    NamingConvention namingConvention = NamingConvention.CAMEL_CASE;

    public enum NamingConvention {
        CAMEL_CASE,
        SNAKE_CASE,
        DOT_CASE
    }
}
