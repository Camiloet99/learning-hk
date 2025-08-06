package org.inventorysystem.storeservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.config.KafkaTopicsConfig;
import org.inventorysystem.storeservice.event.CategoryCreatedEvent;
import org.inventorysystem.storeservice.service.CategoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryListener {

    private final CategoryService categoryService;
    private final KafkaTopicsConfig kafkaTopics;

    @KafkaListener(
            topicPattern = "#{@kafkaTopicsConfig.newCategory}",
            groupId = "store-group",
            containerFactory = "categoryKafkaListenerContainerFactory"
    )
    public void handleCategoryCreated(CategoryCreatedEvent event) {
        log.info("Received CategoryCreatedEvent from Kafka: {}", event);
        categoryService.createFromEvent(event)
                .doOnError(e -> log.error("Failed to process category creation: {}", e.getMessage()))
                .subscribe();
    }
}
