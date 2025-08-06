package org.inventorysystem.inventoryservice.exception;

import lombok.Getter;

@Getter
public class InventoryNotFoundException extends RuntimeException {

    private final String errorCode;

    public InventoryNotFoundException(Long id) {
        super("Inventory item not found with ID: " + id);
        this.errorCode = ErrorCode.INVENTORY_NOT_FOUND_ERROR;
    }
}

