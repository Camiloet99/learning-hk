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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Order Management", description = "Endpoints for managing customer orders")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Creates an order for the given store and user with the provided list of items.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order created successfully",
                            content = @Content(schema = @Schema(implementation = OrderEntity.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping
    public Mono<ResponseEntity<OrderEntity>> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order request payload",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OrderRequest.class)))
            @RequestBody OrderRequest request) {

        log.info("Creating new order for user {} at store {}", request.getUserId(), request.getStoreId());

        return orderService.createOrder(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Failed to create order", e);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @Operation(
            summary = "Get orders by user",
            description = "Retrieves all orders placed by a specific user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved",
                            content = @Content(schema = @Schema(implementation = OrderEntity.class)))
            }
    )
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<OrderEntity>>> getOrdersByUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return orderService.getOrdersByUser(userId)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Get orders by store",
            description = "Retrieves all orders associated with a given store.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved",
                            content = @Content(schema = @Schema(implementation = OrderEntity.class)))
            }
    )
    @GetMapping("/store/{storeId}")
    public Mono<ResponseEntity<List<OrderEntity>>> getOrdersByStore(
            @Parameter(description = "Store ID") @PathVariable Long storeId) {
        return orderService.getOrdersByStore(storeId)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Get order items by order ID",
            description = "Retrieves all items associated with a specific order.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order items retrieved",
                            content = @Content(schema = @Schema(implementation = OrderItemEntity.class)))
            }
    )
    @GetMapping("/{orderId}/items")
    public Mono<ResponseEntity<List<OrderItemEntity>>> getItemsByOrderId(
            @Parameter(description = "Order ID") @PathVariable Long orderId) {
        return orderService.getItemsByOrderId(orderId)
                .map(ResponseEntity::ok);
    }
}

