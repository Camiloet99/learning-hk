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

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacade {

    private final WebClient orderClient;

    public Mono<OrderResponse> sendOrder(OrderRequest orderRequest) {
        return orderClient.post()
                .uri("/api/orders")
                .bodyValue(orderRequest)
                .retrieve()
                .bodyToMono(OrderResponse.class)
                .doOnSuccess(v -> log.info("Order sent successfully to order-service"))
                .doOnError(e -> log.error("Failed to send order: {}", e.getMessage()));
    }

    public Mono<List<OrderResponse>> getOrdersByStore(Long storeId) {
        return orderClient.get()
                .uri("/api/orders/store/{storeId}", storeId)
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .collectList();
    }

    public Mono<List<OrderResponse>> getOrdersByUser(Long userId) {
        return orderClient.get()
                .uri("/api/orders/user/{userId}", userId)
                .retrieve()
                .bodyToFlux(OrderResponse.class)
                .collectList();
    }

    public Mono<List<OrderItemResponse>> getOrderItems(Long orderId) {
        return orderClient.get()
                .uri("/api/orders/{orderId}/items", orderId)
                .retrieve()
                .bodyToFlux(OrderItemResponse.class)
                .collectList();
    }
}

