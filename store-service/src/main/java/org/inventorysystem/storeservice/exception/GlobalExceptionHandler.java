package org.inventorysystem.storeservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the store-service.
 * Catches and handles exceptions across the entire application.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles StoreInventoryNotFoundException when an inventory item is not found in the store DB.
     *
     * @param ex the exception
     * @return 404 Not Found with message
     */
    @ExceptionHandler(StoreInventoryNotFoundException.class)
    public ResponseEntity<String> handleInventoryNotFound(StoreInventoryNotFoundException ex) {
        log.warn("Inventory not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Handles all uncaught exceptions and returns 500 Internal Server Error.
     *
     * @param ex the exception
     * @return 500 Internal Server Error with message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }
}
