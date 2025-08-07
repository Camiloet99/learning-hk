package org.inventorysystem.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new order")
public class OrderRequest {

    @Schema(description = "Store ID", example = "1")
    private Long storeId;

    @Schema(description = "User ID", example = "42")
    private Long userId;

    @Schema(description = "List of order items")
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Item included in an order")
    public static class OrderItemRequest {

        @Schema(description = "Product ID", example = "1001")
        private Long productId;

        @Schema(description = "Quantity to order", example = "2")
        private Integer quantity;
    }
}

