package org.inventorysystem.inventoryservice.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {

    private final String errorCode;

    public InsufficientStockException(Long id, int requested, int available) {
        super("Insufficient stock for product ID " + id +
                ". Requested: " + requested + ", Available: " + available);
        this.errorCode = ErrorCode.INSUFFICIENT_STOCK_ERROR;
    }
}