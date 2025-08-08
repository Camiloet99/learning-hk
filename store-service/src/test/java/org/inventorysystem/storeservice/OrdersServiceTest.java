package org.inventorysystem.storeservice;

import org.inventorysystem.storeservice.event.InventoryUpdatedEvent;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.inventorysystem.storeservice.service.OrdersService;
import org.inventorysystem.storeservice.service.facade.order.OrderFacade;
import org.inventorysystem.storeservice.service.facade.order.request.OrderRequest;
import org.inventorysystem.storeservice.service.facade.order.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrdersServiceTest {

    @Mock
    private StoreInventoryRepository storeInventoryRepository;

    @Mock
    private OrderFacade orderFacade;

    @Mock
    private DatabaseClient databaseClient;

    @InjectMocks
    private OrdersService ordersService;



    @Test
    void createInventoryFromEvent_shouldInsertToDatabase() {
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .price(10.0)
                .categoryId(1L)
                .categoryName("Test")
                .productId(1L)
                .description("Description")
                .newQuantity(20)
                .build();

        DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(ordersService.createInventoryFromEvent(event))
                .verifyComplete();

        verify(spec, times(6)).bind(anyString(), any());
        verify(spec).then();
    }

    @Test
    void placeOrder_shouldCallFacade() {
        OrderRequest request = new OrderRequest(1L, 1L, List.of());
        OrderResponse response = new OrderResponse();

        when(orderFacade.sendOrder(request)).thenReturn(Mono.just(response));

        StepVerifier.create(ordersService.placeOrder(request))
                .expectNext(response)
                .verifyComplete();
    }

}
