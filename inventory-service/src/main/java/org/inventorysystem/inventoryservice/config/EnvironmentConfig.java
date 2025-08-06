package org.inventorysystem.inventoryservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
@Data
public class EnvironmentConfig {

    private Topics topics = new Topics();
    private Retry retry = new Retry();

    @Data
    public static class Topics {
        private String newInventory;
        private String inventoryUpdated;
        private String newCategory;
    }

    @Data
    public static class Retry {
        private int maxAttempts;
        private long delayMs;
    }
}
