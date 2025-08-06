package org.inventorysystem.storeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.event.InventoryUpdatedEvent;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.inventorysystem.storeservice.service.facade.order.OrderFacade;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderItemResponse;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersService {

    private final StoreInventoryRepository storeInventoryRepository;
    private final OrderFacade orderFacade;
    private final DatabaseClient databaseClient;

    /**
     * Handles inventory synchronization from Kafka.
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
                                .quantity(event.getNewQuantity())
                                .price(event.getPrice())
                                .description(event.getDescription())
                                .categoryId(event.getCategoryId())
                                .name(event.getProductName())
                                .id(event.getProductId())
                                .build()))
                .doOnSuccess(updated -> log.info("Inventory synced for productId={}, quantity={}",
                        updated.getId(), updated.getQuantity()))
                .then();
    }

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

    public Mono<OrderResponse> placeOrder(OrderRequest request) {
        return orderFacade.sendOrder(request);
    }

    public Mono<List<OrderResponse>> getOrdersByStore(Long storeId) {
        return orderFacade.getOrdersByStore(storeId);
    }

    public Mono<List<OrderResponse>> getOrdersByUser(Long userId) {
        return orderFacade.getOrdersByUser(userId);
    }

    public Mono<List<OrderItemResponse>> getOrderItems(Long orderId) {
        return orderFacade.getOrderItems(orderId);
    }
}
