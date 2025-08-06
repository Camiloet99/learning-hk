package org.inventorysystem.storeservice.controller;

import lombok.RequiredArgsConstructor;
import org.inventorysystem.storeservice.service.OrdersService;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderItemResponse;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;

    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> placeOrder(@RequestBody OrderRequest request) {
        return ordersService.placeOrder(request)
                .thenReturn(ResponseEntity.ok().build());
    }

    @GetMapping("/store/{storeId}")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByStore(@PathVariable Long storeId) {
        return ordersService.getOrdersByStore(storeId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByUser(@PathVariable Long userId) {
        return ordersService.getOrdersByUser(userId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{orderId}/items")
    public Mono<ResponseEntity<List<OrderItemResponse>>> getOrderItems(@PathVariable Long orderId) {
        return ordersService.getOrderItems(orderId)
                .map(ResponseEntity::ok);
    }

}

