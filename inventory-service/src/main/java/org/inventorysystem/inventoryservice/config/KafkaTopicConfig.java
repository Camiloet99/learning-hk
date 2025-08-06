package org.inventorysystem.inventoryservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newInventoryTopic(
            @Value("${app.kafka.topics.new-inventory}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic inventoryUpdatedTopic(
            @Value("${app.kafka.topics.inventory-updated}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic newCategoryTopic(
            @Value("${app.kafka.topics.new-category}") String topicName) {
        return TopicBuilder.name(topicName).partitions(1).replicas(1).build();
    }
}