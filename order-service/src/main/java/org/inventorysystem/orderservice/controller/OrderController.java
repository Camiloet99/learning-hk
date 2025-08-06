package org.inventorysystem.orderservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.orderservice.dto.OrderRequest;
import org.inventorysystem.orderservice.entity.OrderEntity;
import org.inventorysystem.orderservice.entity.OrderItemEntity;
import org.inventorysystem.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for managing orders.
 * Provides endpoints for creating orders and querying them by user or store.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a new order for the given request, validates stock, and persists the order and its items.
     *
     * @param request the order request including store, user, and product details
     * @return the created order
     */
    @PostMapping
    public Mono<ResponseEntity<OrderEntity>> createOrder(@RequestBody OrderRequest request) {
        log.info("Creating new order for user {} at store {}", request.getUserId(), request.getStoreId());

        return orderService.createOrder(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to create order", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Retrieves all orders placed by a given user.
     *
     * @param userId the ID of the user
     * @return a Flux wrapped in ResponseEntity
     */
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<OrderEntity>>> getOrdersByUser(@PathVariable Long userId) {
        return orderService.getOrdersByUser(userId)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves all orders associated with a given store.
     *
     * @param storeId the ID of the store
     * @return a Flux wrapped in ResponseEntity
     */
    @GetMapping("/store/{storeId}")
    public Mono<ResponseEntity<List<OrderEntity>>> getOrdersByStore(@PathVariable Long storeId) {
        return orderService.getOrdersByStore(storeId)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves all items belonging to a specific order.
     *
     * @param orderId the ID of the order
     * @return a Flux wrapped in ResponseEntity
     */
    @GetMapping("/{orderId}/items")
    public Mono<ResponseEntity<List<OrderItemEntity>>> getItemsByOrderId(@PathVariable Long orderId) {
        return orderService.getItemsByOrderId(orderId)
                .map(ResponseEntity::ok);
    }
}
