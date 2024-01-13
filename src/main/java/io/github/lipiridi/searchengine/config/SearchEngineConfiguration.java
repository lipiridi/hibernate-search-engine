package io.github.lipiridi.searchengine.config;

import io.github.lipiridi.searchengine.SearchService;
import jakarta.persistence.EntityManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SearchEngineProperties.class)
public class SearchEngineConfiguration {

    @Bean
    @ConditionalOnBean(EntityManager.class)
    @ConditionalOnMissingBean
    public SearchService searchService(EntityManager entityManager, SearchEngineProperties searchEngineProperties) {
        return new SearchService(entityManager, searchEngineProperties);
    }
}
