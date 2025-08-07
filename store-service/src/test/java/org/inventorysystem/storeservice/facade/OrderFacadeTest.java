package org.inventorysystem.storeservice.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.inventorysystem.storeservice.service.facade.order.OrderFacade;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderItemResponse;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    private WebClient webClient;
    private OrderFacade orderFacade;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setup() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();


        String baseUrl = mockWebServer.url("/").toString();
        webClient = WebClient.builder().baseUrl(baseUrl).build();
        orderFacade = new OrderFacade(webClient);
    }

    @AfterEach
    void teardown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void sendOrder_shouldReturnOrderResponse() throws Exception {
        OrderResponse mockResponse = new OrderResponse(); // Add mock fields if needed
        String responseJson = new ObjectMapper().writeValueAsString(mockResponse);

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        OrderRequest request = new OrderRequest(); // Populate if needed

        StepVerifier.create(orderFacade.sendOrder(request))
                .expectNextMatches(resp -> resp != null)
                .verifyComplete();
    }

    @Test
    void getOrdersByStore_shouldReturnList() throws Exception {
        OrderResponse mockResponse = new OrderResponse();
        String responseJson = new ObjectMapper().writeValueAsString(List.of(mockResponse));

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(orderFacade.getOrdersByStore(1L))
                .expectNextMatches(list -> !list.isEmpty())
                .verifyComplete();
    }

    @Test
    void getOrdersByUser_shouldReturnList() throws Exception {
        OrderResponse mockResponse = new OrderResponse();
        String responseJson = new ObjectMapper().writeValueAsString(List.of(mockResponse));

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(orderFacade.getOrdersByUser(1L))
                .expectNextMatches(list -> !list.isEmpty())
                .verifyComplete();
    }

    @Test
    void getOrderItems_shouldReturnList() throws Exception {
        OrderItemResponse mockResponse = new OrderItemResponse();
        String responseJson = new ObjectMapper().writeValueAsString(List.of(mockResponse));

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseJson)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(orderFacade.getOrderItems(123L))
                .expectNextMatches(list -> !list.isEmpty())
                .verifyComplete();
    }
}
