package org.inventorysystem.storeservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka.topics")
@Data
public class KafkaTopicsConfig {
    private String newInventory;
    private String inventoryUpdated;
    private String newCategory;
}

