package org.inventorysystem.storeservice.exception;

/**
 * Exception thrown when a requested inventory item is not found in the store's local database.
 */
public class StoreInventoryNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with a message indicating the missing product ID.
     *
     * @param productId The ID of the product that was not found.
     */
    public StoreInventoryNotFoundException(Long productId) {
        super("Inventory for product with ID " + productId + " not found in store database.");
    }

    /**
     * Constructs a new exception with a custom message.
     *
     * @param message The custom message for the exception.
     */
    public StoreInventoryNotFoundException(String message) {
        super(message);
    }
}