package org.inventorysystem.storeservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.config.KafkaTopicsConfig;
import org.inventorysystem.storeservice.event.InventoryUpdatedEvent;
import org.inventorysystem.storeservice.service.OrdersService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StoreInventoryListener {

    private final OrdersService ordersService;
    private final KafkaTopicsConfig kafkaTopics;

    @KafkaListener(
            topicPattern = "#{@kafkaTopicsConfig.inventoryUpdated}",
            groupId = "store-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleInventoryUpdate(InventoryUpdatedEvent event) {
        log.info("Received InventoryUpdatedEvent from Kafka: {}", event);
        ordersService.syncInventoryFromEvent(event)
                .doOnError(e -> log.error("Failed to process inventory update: {}", e.getMessage()))
                .subscribe();
    }

    @KafkaListener(
            topicPattern = "#{@kafkaTopicsConfig.newInventory}",
            groupId = "store-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleNewInventory(InventoryUpdatedEvent event) {
        log.info("Received NEW Inventory Event from Kafka: {}", event);
        ordersService.createInventoryFromEvent(event)
                .doOnError(e -> log.error("Failed to save new inventory: {}", e.getMessage()))
                .subscribe();
    }
}

