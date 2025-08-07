package org.inventorysystem.inventoryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a product")
public class CreateProductRequest {

    @Schema(description = "Name of the product", example = "Smartphone", required = true)
    private String name;

    @Schema(description = "Price of the product", example = "999.99", required = true)
    private Double price;

    @Schema(description = "ID of the category the product belongs to", example = "1", required = true)
    private Long categoryId;

    @Schema(description = "Initial quantity in stock", example = "10", required = true)
    private Integer quantity;

    @Schema(description = "Description of the product", example = "Latest model with 5G")
    private String description;
}
