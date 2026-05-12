package com.logisticsapplication.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class RenderPostgresUrlEnvironmentPostProcessor
        implements EnvironmentPostProcessor, Ordered {

    private static final String DATASOURCE_URL_KEY = "spring.datasource.url";
    private static final String PROPERTY_SOURCE_NAME = "renderPostgresUrlOverride";

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            SpringApplication application
    ) {
        String datasourceUrl = environment.getProperty(DATASOURCE_URL_KEY);
        if (datasourceUrl == null || datasourceUrl.isBlank()) {
            return;
        }

        String normalizedUrl = normalizeDatasourceUrl(datasourceUrl);
        if (normalizedUrl.equals(datasourceUrl)) {
            return;
        }

        Map<String, Object> overrides = new LinkedHashMap<>();
        overrides.put(DATASOURCE_URL_KEY, normalizedUrl);
        environment.getPropertySources().addFirst(
                new MapPropertySource(PROPERTY_SOURCE_NAME, overrides)
        );
    }

    private String normalizeDatasourceUrl(String datasourceUrl) {
        if (datasourceUrl.startsWith("postgresql://")) {
            return "jdbc:" + datasourceUrl;
        }
        if (datasourceUrl.startsWith("postgres://")) {
            return "jdbc:postgresql://" + datasourceUrl.substring("postgres://".length());
        }
        return datasourceUrl;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
