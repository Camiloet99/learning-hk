package org.inventorysystem.inventoryservice;

import org.inventorysystem.inventoryservice.config.EnvironmentConfig;
import org.inventorysystem.inventoryservice.dto.request.CreateCategoryRequest;
import org.inventorysystem.inventoryservice.dto.request.CreateProductRequest;
import org.inventorysystem.inventoryservice.dto.response.ValidateStockResponse;
import org.inventorysystem.inventoryservice.entity.CategoryEntity;
import org.inventorysystem.inventoryservice.entity.ProductEntity;
import org.inventorysystem.inventoryservice.event.InventoryUpdatedEvent;
import org.inventorysystem.inventoryservice.exception.InsufficientStockException;
import org.inventorysystem.inventoryservice.exception.InventoryNotFoundException;
import org.inventorysystem.inventoryservice.event.CategoryCreatedEvent;
import org.inventorysystem.inventoryservice.kafka.KafkaPublisherService;
import org.inventorysystem.inventoryservice.repository.CategoryRepository;
import org.inventorysystem.inventoryservice.repository.ProductRepository;
import org.inventorysystem.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private KafkaPublisherService kafkaPublisherService;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setup() {
        EnvironmentConfig.Topics topics = new EnvironmentConfig.Topics();
        topics.setNewInventory("new-inventory-topic");
        topics.setInventoryUpdated("inventory-updated-topic");
        topics.setNewCategory("new-category-topic");

        EnvironmentConfig realConfig = new EnvironmentConfig();
        realConfig.setTopics(topics);

        inventoryService = new InventoryService(
                productRepository,
                categoryRepository,
                kafkaPublisherService,
                realConfig
        );
    }

    @Test
    void testCreateProduct_shouldPublishToKafka() {
        CreateProductRequest request = new CreateProductRequest("Product A", 99.99, 1L, 5, "Test product");
        ProductEntity savedProduct = ProductEntity.fromRequest(request);
        savedProduct.setId(100L);

        when(productRepository.save(any())).thenReturn(Mono.just(savedProduct));
        when(categoryRepository.findById(1L)).thenReturn(Mono.just(new CategoryEntity(1L, "Category A")));

        StepVerifier.create(inventoryService.create(request))
                .expectNext(savedProduct)
                .verifyComplete();

        verify(productRepository).save(any());
        verify(kafkaPublisherService).publish(eq("new-inventory-topic"), eq("100"), any(InventoryUpdatedEvent.class));
    }

    @Test
    void testCreateCategory_shouldSaveAndPublish() {
        CreateCategoryRequest request = new CreateCategoryRequest("Luxury");
        CategoryEntity saved = new CategoryEntity(10L, "Luxury");

        when(categoryRepository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(inventoryService.createCategory(request))
                .expectNext(saved)
                .verifyComplete();

        verify(kafkaPublisherService).publish(eq("new-category-topic"), eq("10"), any(CategoryCreatedEvent.class));
    }

    @Test
    void testUpdateQuantity_increase_shouldSucceed() {
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Test Watch")
                .categoryId(2L)
                .quantity(5)
                .price(100.0)
                .build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));
        when(productRepository.save(any())).thenReturn(Mono.just(product));
        when(categoryRepository.findById(2L)).thenReturn(Mono.just(new CategoryEntity(2L, "Mechanical")));

        StepVerifier.create(inventoryService.updateQuantity(1L, 3))
                .expectNextMatches(updated -> updated.getQuantity() == 8)
                .verifyComplete();

        verify(kafkaPublisherService).publish(eq("inventory-updated-topic"), eq("1"), any(InventoryUpdatedEvent.class));
    }

    @Test
    void testUpdateQuantity_withInsufficientStock_shouldFail() {
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Test Watch")
                .categoryId(2L)
                .quantity(2)
                .build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(inventoryService.updateQuantity(1L, -5))
                .expectError(InsufficientStockException.class)
                .verify();
    }

    @Test
    void testUpdateQuantity_productNotFound_shouldFail() {
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(inventoryService.updateQuantity(1L, 1))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

    @Test
    void testValidateStock_sufficient() {
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Item")
                .quantity(10)
                .build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(inventoryService.validateStock(product.getId(), 5))
                .expectNextMatches(ValidateStockResponse::getIsValid)
                .verifyComplete();
    }

    @Test
    void testValidateStock_insufficient() {
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Item")
                .quantity(3)
                .build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(inventoryService.validateStock(1L, 10))
                .expectNextMatches(res -> !res.getIsValid())
                .verifyComplete();
    }

    @Test
    void testGetById_shouldReturnProduct() {
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Rolex Submariner")
                .quantity(3)
                .build();

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(inventoryService.getById(1L))
                .expectNextMatches(p -> p.getId().equals(1L) && p.getName().equals("Rolex Submariner"))
                .verifyComplete();
    }

    @Test
    void testGetById_notFound_shouldThrow() {
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(inventoryService.getById(1L))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

    @Test
    void testGetByCategoryId_shouldReturnList() {
        List<ProductEntity> productList = List.of(
                ProductEntity.builder().id(1L).name("Omega Seamaster").categoryId(10L).build(),
                ProductEntity.builder().id(2L).name("Tag Heuer Carrera").categoryId(10L).build()
        );

        when(productRepository.findByCategoryId(10L)).thenReturn(Flux.fromIterable(productList));

        StepVerifier.create(inventoryService.getByCategoryId(10L))
                .expectNextMatches(list -> list.size() == 2 &&
                        list.get(0).getName().equals("Omega Seamaster") &&
                        list.get(1).getId().equals(2L))
                .verifyComplete();
    }

    @Test
    void testGetByCategoryId_notFound_shouldThrow() {
        when(productRepository.findByCategoryId(10L)).thenReturn(Flux.empty());

        StepVerifier.create(inventoryService.getByCategoryId(10L))
                .expectError(InventoryNotFoundException.class)
                .verify();
    }

}
