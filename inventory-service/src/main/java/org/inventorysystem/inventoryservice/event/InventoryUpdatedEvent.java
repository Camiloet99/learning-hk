package org.inventorysystem.inventoryservice.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
