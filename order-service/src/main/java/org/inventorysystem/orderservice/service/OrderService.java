package org.inventorysystem.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.orderservice.dto.OrderRequest;
import org.inventorysystem.orderservice.entity.OrderEntity;
import org.inventorysystem.orderservice.entity.OrderItemEntity;
import org.inventorysystem.orderservice.exception.OrderNotCompletedException;
import org.inventorysystem.orderservice.exception.OrderNotFoundException;
import org.inventorysystem.orderservice.repository.OrderItemRepository;
import org.inventorysystem.orderservice.repository.OrderRepository;
import org.inventorysystem.orderservice.service.facade.InventoryFacade;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service responsible for handling business logic related to order creation and queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryFacade inventoryFacade;

    /**
     * Creates a new order:
     * 1. Saves the order entity.
     * 2. Validates and reserves stock for all requested items.
     * 3. Saves the individual order items.
     *
     * @param request Order creation request containing storeId, userId, and items.
     * @return Mono emitting the created OrderEntity.
     */
    public Mono<OrderEntity> createOrder(OrderRequest request) {
        OrderEntity newOrder = OrderEntity.builder()
                .createdAt(LocalDateTime.now())
                .storeId(request.getStoreId())
                .userId(request.getUserId())
                .build();

        return orderRepository.save(newOrder)
                .doOnSuccess(o -> log.info("Order base saved for storeId={}, userId={}, orderId={}",
                        o.getStoreId(), o.getUserId(), o.getId()))

                .flatMap(savedOrder -> inventoryFacade.validateAndReserveStock(request.getItems())
                        .then(saveOrderItems(savedOrder.getId(), request.getItems()))
                        .thenReturn(savedOrder)
                        .doOnSuccess(o -> log.info("Order completed successfully for orderId={}", o.getId()))
                        .onErrorResume(error -> {
                            log.error("Failed saving order items, rolling back inventory. Error: {}", error.getMessage());
                            return rollbackInventory(request.getItems())
                                    .then(Mono.error(error));
                        }))

                .onErrorResume(e -> {
                    OrderNotCompletedException wrapped =
                            new OrderNotCompletedException("Could not complete order: " + e.getMessage(), e);
                    log.error("[{}] {}", wrapped.getErrorCode(), wrapped.getMessage(), e);
                    return Mono.error(wrapped);
                });
    }

    private Mono<Void> rollbackInventory(List<OrderRequest.OrderItemRequest> items) {
        return Flux.fromIterable(items)
                .flatMap(item -> inventoryFacade.increaseStock(item.getProductId(), item.getQuantity())
                        .doOnSuccess(p ->
                                log.info("Rolled back stock for productId={}, amount={}", item.getProductId(), item.getQuantity()))
                        .onErrorResume(e -> {
                            log.error("Failed to rollback stock for productId={}: {}", item.getProductId(), e.getMessage());
                            return Mono.empty();
                        }))
                .then();
    }

    /**
     * Saves the list of order items related to a specific order.
     *
     * @param orderId ID of the parent order.
     * @param items   List of item requests to convert and persist.
     * @return Flux emitting each saved OrderItemEntity.
     */
    private Mono<List<OrderItemEntity>> saveOrderItems(Long orderId, List<OrderRequest.OrderItemRequest> items) {
        return Flux.fromIterable(items)
                .map(item -> new OrderItemEntity(null, orderId, item.getProductId(), item.getQuantity()))
                .flatMap(orderItemRepository::save)
                .doOnNext(item -> log.info("Saved order item: orderId={}, productId={}, quantity={}",
                        item.getOrderId(), item.getProductId(), item.getQuantity()))
                .collectList();
    }

    /**
     * Retrieves all orders made by a specific user.
     *
     * @param userId ID of the user.
     * @return Flux of OrderEntity instances.
     */
    public Mono<List<OrderEntity>> getOrdersByUser(Long userId) {
        log.info("Fetching orders for userId={}", userId);
        return orderRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException()))
                .collectList();
    }

    /**
     * Retrieves all orders placed in a specific store.
     *
     * @param storeId ID of the store.
     * @return Flux of OrderEntity instances.
     */
    public Mono<List<OrderEntity>> getOrdersByStore(Long storeId) {
        log.info("Fetching orders for storeId={}", storeId);
        return orderRepository.findByStoreId(storeId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException()))
                .collectList();
    }

    /**
     * Retrieves all items associated with a given order.
     *
     * @param orderId ID of the order.
     * @return Mono with the list of OrderItemEntity instances or error if not found.
     * @throws OrderNotFoundException if no items are found for the given orderId.
     */
    public Mono<List<OrderItemEntity>> getItemsByOrderId(Long orderId) {
        log.info("Fetching items for orderId={}", orderId);
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
                .flatMap(o -> orderItemRepository.findByOrderId(orderId).collectList());
    }
}

