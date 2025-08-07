package org.inventorysystem.inventoryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a product category")
public class CreateCategoryRequest {

    @Schema(description = "Name of the category", example = "Electronics", required = true)
    private String name;
}
