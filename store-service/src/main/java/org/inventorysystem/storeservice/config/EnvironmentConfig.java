package org.inventorysystem.storeservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class EnvironmentConfig {
    private Domains domains;

    @Data
    public static class Domains {
        private String order;
    }
}