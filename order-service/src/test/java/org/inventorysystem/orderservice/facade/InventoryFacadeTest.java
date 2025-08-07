package org.inventorysystem.orderservice.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.inventorysystem.orderservice.config.EnvironmentConfig;
import org.inventorysystem.orderservice.dto.OrderRequest;
import org.inventorysystem.orderservice.exception.InsufficientStockException;
import org.inventorysystem.orderservice.service.facade.InventoryFacade;
import org.inventorysystem.orderservice.service.facade.response.ProductResponse;
import org.inventorysystem.orderservice.service.facade.response.ValidateStockResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryFacadeTest {

    private InventoryFacade inventoryFacade;

    private WebClient webClient;

    @Mock
    private ExchangeFunction exchangeFunction;

    private EnvironmentConfig environmentConfig;

    @BeforeEach
    void setUp() {
        // Setup real EnvironmentConfig manually
        environmentConfig = new EnvironmentConfig();
        EnvironmentConfig.Domains domains = new EnvironmentConfig.Domains();
        domains.setInventory("http://inventory-service/api/inventory");
        environmentConfig.setDomains(domains);

        EnvironmentConfig.Retry retry = new EnvironmentConfig.Retry();
        retry.setMaxAttempts(3);
        retry.setDelayMs(100);
        environmentConfig.setRetry(retry);

        // Create WebClient with mocked ExchangeFunction
        webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();

        inventoryFacade = new InventoryFacade(webClient, environmentConfig);
    }

    @Test
    void validateAndReserveStock_shouldSucceed() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 3);
        ValidateStockResponse response = new ValidateStockResponse(true);

        // Mock POST /validate-stock
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse(HttpStatus.OK, response)))
                .thenReturn(Mono.just(mockResponse(HttpStatus.OK, null))); // For PUT decrease

        StepVerifier.create(inventoryFacade.validateAndReserveStock(List.of(item)))
                .verifyComplete();
    }

    @Test
    void validateAndReserveStock_shouldFail_dueToStock() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest(1L, 3);
        ValidateStockResponse response = new ValidateStockResponse(false);

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse(HttpStatus.OK, response))); // POST validate fails

        StepVerifier.create(inventoryFacade.validateAndReserveStock(List.of(item)))
                .expectError(InsufficientStockException.class)
                .verify();
    }

    @Test
    void increaseStock_shouldSucceed() {
        ProductResponse response = ProductResponse.builder()
                .id(1L)
                .quantity(10)
                .build();

        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenReturn(Mono.just(mockResponse(HttpStatus.OK, response)));

        StepVerifier.create(inventoryFacade.increaseStock(1L, 5))
                .expectNextMatches(p -> p.getQuantity() == 10)
                .verifyComplete();
    }

    // Utilidad para simular respuesta JSON
    private <T> ClientResponse mockResponse(HttpStatus status, T body) {
        return ClientResponse.create(status)
                .header("Content-Type", "application/json")
                .body(body == null ? "" : toJson(body))
                .build();
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON conversion failed", e);
        }
    }
}
