package io.github.lipiridi.searchengine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jpa.hibernate.search-engine")
public class SearchEngineProperties {

    private int maxPageSize = 100;
    private NamingConvention namingConvention = NamingConvention.CAMEL_CASE;

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

    public enum NamingConvention {
        CAMEL_CASE,
        SNAKE_CASE,
        DOT_CASE
    }
}
