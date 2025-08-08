package org.inventorysystem.storeservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.util.stream.Collectors;

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
     * Handles WebExchangeBindException (validation errors in @Valid request bodies).
     *
     * @param ex the exception
     * @return 400 Bad Request with validation error details
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleValidationErrors(WebExchangeBindException ex) {
        String errors = ex.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation failed: " + errors);
    }

    /**
     * Handles ServerWebInputException (e.g., when a Long is expected but a String is passed).
     *
     * @param ex the exception
     * @return 400 Bad Request with message
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<String> handleServerWebInput(ServerWebInputException ex) {
        log.warn("Bad input: {}", ex.getReason());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid input: " + ex.getReason());
    }

    /**
     * Handles DecodingException (e.g., malformed JSON).
     *
     * @param ex the exception
     * @return 400 Bad Request with message
     */
    @ExceptionHandler(DecodingException.class)
    public ResponseEntity<String> handleDecodingException(DecodingException ex) {
        log.warn("Malformed JSON body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Malformed JSON body: " + ex.getMessage());
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
