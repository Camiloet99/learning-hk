package org.inventorysystem.orderservice.service.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.orderservice.config.EnvironmentConfig;
import org.inventorysystem.orderservice.dto.OrderRequest;
import org.inventorysystem.orderservice.exception.InsufficientStockException;
import org.inventorysystem.orderservice.service.facade.response.ProductResponse;
import org.inventorysystem.orderservice.service.facade.response.ValidateStockResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.*;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.List;

/**
 * Facade responsible for communicating with the inventory-service.
 * Handles stock validation and reduction.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryFacade {

    private final WebClient inventoryClient;
    private final EnvironmentConfig environmentConfig;

    /**
     * Validates and reserves stock for a list of order items by:
     * 1. Validating availability via external inventory service.
     * 2. Decreasing stock if available.
     * Fails fast if any item has insufficient stock or validation error.
     *
     * @param items List of order items.
     * @return Mono<Void> indicating success or propagating error.
     */
    public Mono<Void> validateAndReserveStock(List<OrderRequest.OrderItemRequest> items) {
        return Flux.fromIterable(items)
                .flatMap(item -> validateStock(item)
                        .flatMap(response -> {
                            if (Boolean.TRUE.equals(response.getIsValid())) {
                                return decreaseStock(item);
                            } else {
                                String msg = String.format("Insufficient stock for productId=%d", item.getProductId());
                                log.warn(msg);
                                return Mono.error(new InsufficientStockException(item.getProductId(), item.getQuantity(), 0));
                            }
                        }))
                .then();
    }

    /**
     * Validates stock for a single product.
     *
     * @param item Order item to validate.
     * @return Mono indicating completion or error.
     */
    private Mono<ValidateStockResponse> validateStock(OrderRequest.OrderItemRequest item) {
        return inventoryClient.post()
                .uri(environmentConfig.getDomains().getInventory() + "/api/inventory/validate-stock")
                .bodyValue(item)
                .retrieve()
                .bodyToMono(ValidateStockResponse.class)
                .doOnSuccess(response ->
                        log.info("Stock validated for productId={}", item.getProductId()))
                .doOnError(error ->
                        log.warn("Validation failed for productId={} with error: {}",
                                item.getProductId(), error.getMessage()))
                .retryWhen(retrySpec("Stock validation", String.valueOf(item.getProductId())));
    }

    /**
     * Decreases stock for a single product.
     *
     * @param item Order item to decrease stock for.
     * @return Mono indicating completion or error.
     */
    private Mono<Void> decreaseStock(OrderRequest.OrderItemRequest item) {
        return inventoryClient.put()
                .uri(environmentConfig.getDomains().getInventory() + "/api/inventory/" + item.getProductId() + "/decrease?amount=" + item.getQuantity())
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(response ->
                        log.info("Stock decreased for productId={}, quantity={}", item.getProductId(), item.getQuantity()))
                .doOnError(error ->
                        log.warn("Decrease failed for productId={} with error: {}",
                                item.getProductId(), error.getMessage()))
                .retryWhen(retrySpec("Stock decrease", String.valueOf(item.getProductId())));
    }

    public Mono<ProductResponse> increaseStock(Long productId, int amount) {
        return inventoryClient.put()
                .uri("/api/inventory/{id}/increase?amount={amount}", productId, amount)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .doOnSuccess(p -> log.info("Increased stock for productId={}, new quantity={}", productId, p.getQuantity()))
                .doOnError(e -> log.error("Failed to increase stock for productId={}: {}", productId, e.getMessage()))
                .retryWhen(retrySpec("Stock increase", String.valueOf(productId)));
    }

    /**
     * Builds a reusable RetryBackoffSpec based on configuration.
     *
     * @return RetryBackoffSpec with filtering and logging.
     */
    private RetryBackoffSpec retrySpec(String context, String identifier) {
        return Retry.backoff(environmentConfig.getRetry().getMaxAttempts(),
                        Duration.ofMillis(environmentConfig.getRetry().getDelayMs()))
                .filter(this::isRetryable)
                .doBeforeRetry(retrySignal ->
                        log.warn("Retrying [{}] for {} due to error: {}", context, identifier, retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((retryBackoffSpec, signal) ->
                        new RuntimeException(context + " failed after retries for " + identifier));
    }

    /**
     * Determines whether a given exception is retryable.
     *
     * @param throwable The exception to evaluate.
     * @return true if the error is retryable; false otherwise.
     */
    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof WebClientResponseException ex) {
            return ex.getStatusCode().is5xxServerError(); // Retry only on server errors
        }
        return true; // Retry on unexpected exceptions
    }
}
