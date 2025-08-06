package org.inventorysystem.inventoryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    private String name;
    private Double price;
    private Long categoryId;
    private Integer quantity;
    private String description;

}
