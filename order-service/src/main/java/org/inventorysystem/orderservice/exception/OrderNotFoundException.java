package org.inventorysystem.orderservice.exception;

import lombok.Getter;

@Getter
public class OrderNotFoundException extends RuntimeException {

    private final String errorCode;

    public OrderNotFoundException(Long id) {
        super("Order with ID " + id + " not found.");
        this.errorCode = ErrorCode.ORDER_NOT_FOUND;
    }

    public OrderNotFoundException() {
        super("Order not found.");
        this.errorCode = ErrorCode.ORDER_NOT_FOUND;
    }
}
