package org.inventorysystem.storeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Orders", description = "Operations related to orders")
public class OrdersController {

    private final OrdersService ordersService;

    @Operation(
            summary = "Place a new order",
            description = "Creates a new order with the specified items, user, and store."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> placeOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order request with user ID, store ID and list of items",
                    required = true
            )
            @RequestBody OrderRequest request) {
        return ordersService.placeOrder(request)
                .thenReturn(ResponseEntity.ok().build());
    }

    @Operation(
            summary = "Get orders by store ID",
            description = "Retrieves a list of orders made at the specified store."
    )
    @ApiResponse(responseCode = "200", description = "List of orders for the store")
    @GetMapping("/store/{storeId}")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByStore(
            @Parameter(description = "Store ID", required = true)
            @PathVariable Long storeId) {
        return ordersService.getOrdersByStore(storeId)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Get orders by user ID",
            description = "Retrieves a list of orders made by the specified user."
    )
    @ApiResponse(responseCode = "200", description = "List of orders for the user")
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<List<OrderResponse>>> getOrdersByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        return ordersService.getOrdersByUser(userId)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Get items by order ID",
            description = "Retrieves all items associated with the specified order."
    )
    @ApiResponse(responseCode = "200", description = "List of items in the order")
    @GetMapping("/{orderId}/items")
    public Mono<ResponseEntity<List<OrderItemResponse>>> getOrderItems(
            @Parameter(description = "Order ID", required = true)
            @PathVariable Long orderId) {
        return ordersService.getOrderItems(orderId)
                .map(ResponseEntity::ok);
    }
}

