package org.inventorysystem.inventoryservice.exception;

import lombok.Getter;

@Getter
public class KafkaPublishException extends RuntimeException {

    private final String errorCode;

    public KafkaPublishException(String message) {
        super(message);
        this.errorCode = ErrorCode.KAFKA_PUBLISH_ERROR;
    }

    public KafkaPublishException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.KAFKA_PUBLISH_ERROR;
    }
}

