package org.inventorysystem.orderservice.exception;

import lombok.Getter;

@Getter
public class OrderNotCompletedException extends RuntimeException {

    private final String errorCode;

    public OrderNotCompletedException(String message) {
        super(message);
        this.errorCode = ErrorCode.ORDER_NOT_COMPLETED_ERROR;
    }

    public OrderNotCompletedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.ORDER_NOT_COMPLETED_ERROR;
    }

}
