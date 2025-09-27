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
        DOT_CASE;

        public String mergeStrings(String... strings) {
            StringBuilder sb = new StringBuilder(strings[0]);
            for (int i = 1; i < strings.length; i++) {
                switch (this) {
                    case CAMEL_CASE -> sb.append(capitalize(strings[i]));
                    case SNAKE_CASE -> sb.append("_").append(strings[i]);
                    case DOT_CASE -> sb.append(".").append(strings[i]);
                }
            }

            return sb.toString();
        }

        public String formatId(String id) {
            return switch (this) {
                case CAMEL_CASE -> id;
                case SNAKE_CASE -> id.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
                case DOT_CASE -> id.replaceAll("([a-z0-9])([A-Z])", "$1.$2").toLowerCase();
            };
        }

        private String capitalize(String input) {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
    }
}
