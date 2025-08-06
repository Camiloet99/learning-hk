package org.inventorysystem.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.inventorysystem.inventoryservice.dto.CreateCategoryRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("categories")
public class CategoryEntity {
    @Id
    private Long id;
    private String name;

    public static CategoryEntity fromRequest(CreateCategoryRequest request) {
        return CategoryEntity.builder()
                .name(request.getName())
                .build();
    }
}
