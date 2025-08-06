package org.inventorysystem.inventoryservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for validating stock availability of a specific product and quantity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockValidationRequest {

    /**
     * The ID of the product to check stock for.
     * Must not be null.
     */
    @NotNull(message = "Product ID must not be null")
    private Long productId;

    /**
     * The quantity requested.
     * Must be a positive integer.
     */
    @NotNull(message = "Requested quantity must not be null")
    @Positive(message = "Requested quantity must be greater than zero")
    private Integer quantity;
}