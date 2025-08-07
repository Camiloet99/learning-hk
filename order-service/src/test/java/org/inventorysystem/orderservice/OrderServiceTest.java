package org.inventorysystem.orderservice;

import org.inventorysystem.orderservice.dto.OrderRequest;
import org.inventorysystem.orderservice.entity.OrderEntity;
import org.inventorysystem.orderservice.entity.OrderItemEntity;
import org.inventorysystem.orderservice.exception.OrderNotCompletedException;
import org.inventorysystem.orderservice.exception.OrderNotFoundException;
import org.inventorysystem.orderservice.repository.OrderItemRepository;
import org.inventorysystem.orderservice.repository.OrderRepository;
import org.inventorysystem.orderservice.service.OrderService;
import org.inventorysystem.orderservice.service.facade.InventoryFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private InventoryFacade inventoryFacade;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest request;
    private OrderEntity savedOrder;

    @BeforeEach
    void setup() {
        request = new OrderRequest(
                1L,
                42L,
                List.of(new OrderRequest.OrderItemRequest(100L, 3))
        );

        savedOrder = OrderEntity.builder()
                .id(99L)
                .storeId(1L)
                .userId(42L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createOrder_success() {
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));
        when(inventoryFacade.validateAndReserveStock(any())).thenReturn(Mono.empty());
        when(orderItemRepository.save(any())).thenReturn(Mono.just(new OrderItemEntity(1L, 99L, 100L, 3)));

        StepVerifier.create(orderService.createOrder(request))
                .expectNext(savedOrder)
                .verifyComplete();

        verify(orderRepository).save(any());
        verify(orderItemRepository).save(any());
        verify(inventoryFacade).validateAndReserveStock(request.getItems());
    }

    @Test
    void createOrder_inventoryFails_shouldRollback() {
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));
        when(inventoryFacade.validateAndReserveStock(any())).thenReturn(Mono.error(new RuntimeException("Inventory error")));
        when(inventoryFacade.increaseStock(100L, 3)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder(request))
                .expectError(OrderNotCompletedException.class)
                .verify();

        verify(inventoryFacade).increaseStock(100L, 3);
    }

    @Test
    void getOrdersByUser_found() {
        OrderEntity order1 = OrderEntity.builder().id(1L).userId(42L).storeId(1L).createdAt(LocalDateTime.now()).build();

        when(orderRepository.findByUserId(42L)).thenReturn(Flux.just(order1));

        StepVerifier.create(orderService.getOrdersByUser(42L))
                .expectNext(List.of(order1))
                .verifyComplete();
    }

    @Test
    void getOrdersByUser_notFound() {
        when(orderRepository.findByUserId(42L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrdersByUser(42L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void getOrdersByStore_found() {
        OrderEntity order = OrderEntity.builder().id(1L).storeId(1L).userId(5L).createdAt(LocalDateTime.now()).build();

        when(orderRepository.findByStoreId(1L)).thenReturn(Flux.just(order));

        StepVerifier.create(orderService.getOrdersByStore(1L))
                .expectNext(List.of(order))
                .verifyComplete();
    }

    @Test
    void getOrdersByStore_notFound() {
        when(orderRepository.findByStoreId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getOrdersByStore(1L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void getItemsByOrderId_found() {
        OrderEntity order = OrderEntity.builder().id(10L).storeId(1L).userId(1L).createdAt(LocalDateTime.now()).build();
        OrderItemEntity item = new OrderItemEntity(1L, 10L, 100L, 2);

        when(orderRepository.findById(10L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(10L)).thenReturn(Flux.just(item));

        StepVerifier.create(orderService.getItemsByOrderId(10L))
                .expectNext(List.of(item))
                .verifyComplete();
    }

    @Test
    void getItemsByOrderId_orderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getItemsByOrderId(10L))
                .expectError(OrderNotFoundException.class)
                .verify();
    }
}

