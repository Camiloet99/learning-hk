package org.inventorysystem.orderservice.exception;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {


    public InsufficientStockException(Long id, int requested, int available) {
        super("Insufficient stock for product ID " + id +
                ". Requested: " + requested + ", Available: " + available);
    }
}
