package org.inventorysystem.inventoryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to validate stock for a product")
public class StockValidationRequest {

    @NotNull(message = "Product ID must not be null")
    @Schema(description = "Product ID", example = "1", required = true)
    private Long productId;

    @NotNull(message = "Requested quantity must not be null")
    @Positive(message = "Requested quantity must be greater than zero")
    @Schema(description = "Quantity to check availability for", example = "5", required = true)
    private Integer quantity;
}
