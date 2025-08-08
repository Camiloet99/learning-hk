package org.inventorysystem.storeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.event.InventoryUpdatedEvent;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.inventorysystem.storeservice.service.facade.order.OrderFacade;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service layer that handles order placement, retrieval and inventory synchronization logic.
 * Acts as intermediary between Kafka events, database layer, and order service facade.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final OrderFacade orderFacade;
    private final DatabaseClient databaseClient;

    /**
     * Synchronizes the inventory by updating or creating the product locally
     * based on the incoming Kafka event from the inventory service.
     *
     * @param event The inventory update event.
     * @return Mono<Void> indicating completion.
     */
    public Mono<Void> syncInventoryFromEvent(InventoryUpdatedEvent event) {
        return storeInventoryRepository.findById(event.getProductId())
                .flatMap(existing -> {
                    existing.setQuantity(event.getNewQuantity());
                    return storeInventoryRepository.save(existing);
                })
                .switchIfEmpty(storeInventoryRepository.save(
                        ProductEntity.builder()
                                .id(event.getProductId())
                                .name(event.getProductName())
                                .price(event.getPrice())
                                .categoryId(event.getCategoryId())
                                .quantity(event.getNewQuantity())
                                .description(event.getDescription())
                                .build()))
                .doOnSuccess(updated -> log.info("Inventory synced for productId={}, quantity={}",
                        updated.getId(), updated.getQuantity()))
                .then();
    }

    /**
     * Creates a new inventory record directly in the database using a raw SQL insert.
     * Intended for Kafka-based event-driven insertions.
     *
     * @param event The inventory update event containing product details.
     * @return Mono<Void> indicating completion.
     */
    public Mono<Void> createInventoryFromEvent(InventoryUpdatedEvent event) {
        return databaseClient.sql("""
                    INSERT INTO products (id, name, price, category_id, quantity, description)
                    VALUES (:id, :name, :price, :categoryId, :quantity, :description)
                """)
                .bind("id", event.getProductId())
                .bind("name", event.getProductName())
                .bind("price", event.getPrice())
                .bind("categoryId", event.getCategoryId())
                .bind("quantity", event.getNewQuantity())
                .bind("description", event.getDescription())
                .then()
                .doOnSuccess(unused -> log.info("Product inserted with ID: {}", event.getProductId()))
                .doOnError(e -> log.error("Failed to insert product: {}", e.getMessage()));
    }

    /**
     * Sends the order to the order-service via the OrderFacade.
     *
     * @param request OrderRequest containing userId, storeId, and item list.
     * @return Mono with OrderResponse.
     */
    public Mono<OrderResponse> placeOrder(OrderRequest request) {
        return orderFacade.sendOrder(request);
    }
}

