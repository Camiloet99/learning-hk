package org.inventorysystem.storeservice;

import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.exception.StoreInventoryNotFoundException;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.inventorysystem.storeservice.service.ProductsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductsServiceTest {

    @Mock
    private StoreInventoryRepository storeInventoryRepository;

    @InjectMocks
    private ProductsService productsService;

    @Test
    void getInventoryByProductId_shouldReturnProduct_whenFound() {
        ProductEntity product = ProductEntity.builder().id(1L).name("Omega Speedmaster").build();
        when(storeInventoryRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(productsService.getInventoryByProductId(1L))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void getInventoryByProductId_shouldThrow_whenNotFound() {
        when(storeInventoryRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productsService.getInventoryByProductId(1L))
                .expectError(StoreInventoryNotFoundException.class)
                .verify();
    }

    @Test
    void getInventoryByCategoryId_shouldReturnProducts_whenFound() {
        ProductEntity product1 = ProductEntity.builder().id(1L).categoryId(5L).name("Seiko").build();
        ProductEntity product2 = ProductEntity.builder().id(2L).categoryId(5L).name("Tudor").build();

        when(storeInventoryRepository.findByCategoryId(5L)).thenReturn(Flux.just(product1, product2));

        StepVerifier.create(productsService.getInventoryByCategoryId(5L))
                .expectNextMatches(list -> list.size() == 2 && list.contains(product1) && list.contains(product2))
                .verifyComplete();
    }

    @Test
    void getInventoryByCategoryId_shouldThrow_whenEmpty() {
        when(storeInventoryRepository.findByCategoryId(99L)).thenReturn(Flux.empty());

        StepVerifier.create(productsService.getInventoryByCategoryId(99L))
                .expectError(StoreInventoryNotFoundException.class)
                .verify();
    }
}

