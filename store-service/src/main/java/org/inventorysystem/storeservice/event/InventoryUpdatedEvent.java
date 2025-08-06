package org.inventorysystem.storeservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdatedEvent {
    private Long productId;
    private String productName;
    private String description;
    private Integer newQuantity;
    private Long categoryId;
    private String categoryName;
    private InventoryEventType eventType;
    private Double price;
}