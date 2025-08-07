package org.inventorysystem.inventoryservice.entity;

import lombok.*;
import org.inventorysystem.inventoryservice.dto.request.CreateProductRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class ProductEntity {
    @Id
    private Long id;

    private String name;

    private Double price;

    @Column("category_id")
    private Long categoryId;

    private Integer quantity;

    private String description;

    public static ProductEntity fromRequest(CreateProductRequest request) {
        return ProductEntity.builder()
                .categoryId(request.getCategoryId())
                .price(request.getPrice())
                .description(request.getDescription())
                .name(request.getName())
                .quantity(request.getQuantity())
                .build();
    }

}
