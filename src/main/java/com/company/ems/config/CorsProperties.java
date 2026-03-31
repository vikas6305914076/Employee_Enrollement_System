package com.company.ems.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOriginPatterns) {

    public CorsProperties {
        allowedOriginPatterns = allowedOriginPatterns == null
                ? List.of()
                : allowedOriginPatterns.stream()
                .map(String::trim)
                .filter((value) -> !value.isEmpty())
                .toList();
    }
}
