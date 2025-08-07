package org.inventorysystem.storeservice.service.facade.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderItemResponse;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Facade responsible for communicating with the order-service via WebClient.
 * Delegates operations such as placing an order and retrieving order details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final WebClient orderClient;

    /**
     * Sends an order creation request to the order-service.
     *
     * @param orderRequest the order to place.
     * @return Mono emitting the created OrderResponse.
     */
    public Mono<OrderResponse> sendOrder(OrderRequest orderRequest) {
        return orderClient.post()
                .uri("/api/orders")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .doOnSuccess(v -> log.info("Order sent successfully to order-service"))
                .doOnError(e -> log.error("Failed to send order: {}", e.getMessage()));
    }

    /**
     * Retrieves all orders made in a specific store from the order-service.
     *
     * @param storeId ID of the store.
     * @return Mono emitting a list of OrderResponse objects.
     */
    public Mono<List<OrderResponse>> getOrdersByStore(Long storeId) {
        return orderClient.get()
                .uri("/api/orders/store/{storeId}", storeId)
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .collectList();
    }

    /**
     * Retrieves all orders made by a specific user from the order-service.
     *
     * @param userId ID of the user.
     * @return Mono emitting a list of OrderResponse objects.
     */
    public Mono<List<OrderResponse>> getOrdersByUser(Long userId) {
        return orderClient.get()
                .uri("/api/orders/user/{userId}", userId)
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .collectList();
    }

    /**
     * Retrieves all items associated with a specific order from the order-service.
     *
     * @param orderId ID of the order.
     * @return Mono emitting a list of OrderItemResponse objects.
     */
    public Mono<List<OrderItemResponse>> getOrderItems(Long orderId) {
        return orderClient.get()
                .uri("/api/orders/{orderId}/items", orderId)
                .retrieve()
                .bodyToFlux(OrderItemResponse.class)
                .collectList();
    }
}


