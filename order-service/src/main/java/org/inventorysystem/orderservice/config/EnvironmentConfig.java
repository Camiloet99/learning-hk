package org.inventorysystem.orderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class EnvironmentConfig {

    private Domains domains;
    private Retry retry;

    @Data
    public static class Domains {
        private String inventory;
    }

    @Data
    public static class Retry {
        private int maxAttempts;
        private long delayMs;
    }
}
