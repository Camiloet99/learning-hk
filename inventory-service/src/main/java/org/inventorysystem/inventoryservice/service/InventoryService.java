package org.inventorysystem.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.inventoryservice.config.EnvironmentConfig;
import org.inventorysystem.inventoryservice.dto.request.CreateCategoryRequest;
import org.inventorysystem.inventoryservice.dto.request.CreateProductRequest;
import org.inventorysystem.inventoryservice.dto.response.ValidateStockResponse;
import org.inventorysystem.inventoryservice.entity.CategoryEntity;
import org.inventorysystem.inventoryservice.entity.ProductEntity;
import org.inventorysystem.inventoryservice.event.InventoryEventType;
import org.inventorysystem.inventoryservice.event.InventoryUpdatedEvent;
import org.inventorysystem.inventoryservice.exception.ErrorCode;
import org.inventorysystem.inventoryservice.exception.InsufficientStockException;
import org.inventorysystem.inventoryservice.exception.InventoryNotFoundException;
import org.inventorysystem.inventoryservice.exception.KafkaPublishException;
import org.inventorysystem.inventoryservice.kafka.CategoryCreatedEvent;
import org.inventorysystem.inventoryservice.kafka.KafkaPublisherService;
import org.inventorysystem.inventoryservice.repository.CategoryRepository;
import org.inventorysystem.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service layer responsible for handling inventory operations including creation,
 * stock updates, validations, and publishing inventory changes to Kafka.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final KafkaPublisherService kafkaPublisherService;
    private final EnvironmentConfig environmentConfig;

    /**
     * Handles the creation of a new product in the inventory.
     * This method orchestrates the saving of the product entity
     * and the subsequent publication of a creation event to Kafka.
     *
     * @param productRequest The request payload containing product details.
     * @return Mono emitting the created ProductEntity after publishing the Kafka event.
     */
    public Mono<ProductEntity> create(CreateProductRequest productRequest) {
        log.info("Creating inventory item: {}", productRequest.getName());
        return saveProduct(productRequest)
                .flatMap(this::publishCreationEvent);
    }

    /**
     * Handles the creation of a new category in the inventory.
     * This method orchestrates the saving of the category entity
     * and the subsequent publication of a creation event to Kafka.
     *
     * @param categoryRequest The request payload containing category details.
     * @return Mono emitting the created CategoryEntity after publishing the Kafka event.
     */
    public Mono<CategoryEntity> createCategory(CreateCategoryRequest categoryRequest) {
        log.info("Creating category item: {}", categoryRequest.getName());
        return categoryRepository.save(CategoryEntity.fromRequest(categoryRequest))
                .doOnSuccess(saved -> {
                    CategoryCreatedEvent event = CategoryCreatedEvent.builder()
                            .categoryId(saved.getId())
                            .categoryName(saved.getName())
                            .build();

                    String topic = environmentConfig.getTopics().getNewCategory(); // agrega esto a tu config
                    kafkaPublisherService.publish(topic, String.valueOf(saved.getId()), event);
                    log.info("Published category creation event for ID: {}", saved.getId());
                })
                .doOnError(e -> log.error("Failed to create and publish category: {}", categoryRequest.getName(), e));
    }

    /**
     * Persists the new product entity in the database.
     *
     * @param productRequest The product request to be transformed and saved.
     * @return Mono emitting the saved ProductEntity.
     */
    private Mono<ProductEntity> saveProduct(CreateProductRequest productRequest) {
        return productRepository.save(ProductEntity.fromRequest(productRequest))
                .doOnSuccess(p -> log.info("Product created successfully with ID: {}", p.getId()))
                .doOnError(e -> log.error("Error while saving product: {}", productRequest.getName(), e));
    }

    /**
     * Publishes a Kafka event indicating that a new product has been created.
     * The event includes the product details and associated category information.
     *
     * @param product The product entity for which the event is to be published.
     * @return Mono emitting the original ProductEntity after event publication.
     */
    private Mono<ProductEntity> publishCreationEvent(ProductEntity product) {
        return categoryRepository.findById(product.getCategoryId())
                .doOnNext(category -> {
                    InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .newQuantity(product.getQuantity())
                            .productName(product.getName())
                            .productId(product.getId())
                            .eventType(InventoryEventType.CREATED)
                            .build();
                    String topic = environmentConfig.getTopics().getNewInventory();
                    kafkaPublisherService.publish(topic, String.valueOf(product.getId()), event);
                    log.info("Published creation event for product ID: {}", product.getId());
                })
                .doOnError(e -> log.error("Failed to publish Kafka creation event for product ID: {}", product.getId(), e))
                .thenReturn(product);
    }

    /**
     * Updates the quantity of an existing product by applying the delta value.
     * Emits a Kafka event after a successful update.
     *
     * @param id    The ID of the product to update.
     * @param delta The change in quantity (positive to increase, negative to decrease).
     * @return Mono emitting the updated product entity.
     * @throws InventoryNotFoundException if the product does not exist.
     * @throws InsufficientStockException if the resulting quantity would be negative.
     */
    public Mono<ProductEntity> updateQuantity(Long id, int delta) {
        log.info("Updating quantity for product ID: {} with delta: {}", id, delta);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("{} - Product with ID {} not found for quantity update", ErrorCode.INVENTORY_NOT_FOUND_ERROR, id);
                    return Mono.error(new InventoryNotFoundException(id));
                }))
                .flatMap(p -> {
                    int newQty = p.getQuantity() + delta;
                    if (newQty < 0) {
                        log.warn("{} - Insufficient stock for product ID {}. Available: {}, Requested delta: {}",
                                ErrorCode.INSUFFICIENT_STOCK_ERROR, id, p.getQuantity(), delta);
                        return Mono.error(new InsufficientStockException(id, -delta, p.getQuantity()));
                    }
                    p.setQuantity(newQty);
                    log.debug("New quantity for product ID {}: {}", id, newQty);
                    return productRepository.save(p);
                })
                .doOnSuccess(p -> log.info("Product quantity updated successfully for ID: {}", p.getId()))
                .doOnError(e -> log.error("Error updating quantity for product ID: {}", id, e))
                .flatMap(productEntity -> sendUpdateEvent(productEntity, delta));
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id The ID of the product to retrieve.
     * @return Mono emitting the found product.
     * @throws InventoryNotFoundException if the product is not found.
     */
    public Mono<ProductEntity> getById(Long id) {
        log.debug("Fetching inventory item with ID: {}", id);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Product with ID {} not found", id);
                    return Mono.error(new InventoryNotFoundException(id));
                }))
                .doOnSuccess(p -> log.info("Fetched product ID: {}", p.getId()))
                .doOnError(e -> log.error("Error fetching product with ID: {}", id, e));
    }

    /**
     * Retrieves a list of products by its category ID.
     *
     * @param categoryId The ID of the category of the products to retrieve.
     * @return Mono of List emitting the found product.
     * @throws InventoryNotFoundException if the products are not found.
     */
    public Mono<List<ProductEntity>> getByCategoryId(Long categoryId) {
        log.debug("Fetching inventory item with category ID: {}", categoryId);
        return productRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Product with category ID {} not found", categoryId);
                    return Mono.error(new InventoryNotFoundException(categoryId));
                }))
                .doOnError(e -> log.error("Error fetching product with ID: {}", categoryId, e))
                .collectList();
    }

    /**
     * Validates if there is sufficient stock for a given product and quantity.
     *
     * @param productId         The product ID to validate.
     * @param requestedQuantity The quantity requested.
     * @return Mono signaling completion if stock is sufficient.
     * @throws InventoryNotFoundException if the product does not exist.
     * @throws InsufficientStockException if stock is insufficient.
     */
    public Mono<ValidateStockResponse> validateStock(Long productId, int requestedQuantity) {
        log.info("Validating stock for product ID: {}, requested quantity: {}", productId, requestedQuantity);

        return findProductOrFail(productId)
                .flatMap(product -> checkSufficientStock(product, requestedQuantity)
                        .then(Mono.just(
                                ValidateStockResponse.builder()
                                        .isValid(true)
                                        .build())))
                .doOnError(e -> log.error("Error validating stock for product ID: {}", productId, e))
                .onErrorResume(e -> Mono.just(
                        ValidateStockResponse.builder()
                                .isValid(false)
                                .build()));
    }

    /**
     * Finds a product by ID or throws an InventoryNotFoundException.
     *
     * @param productId The ID of the product to find.
     * @return Mono emitting the found product.
     * @throws InventoryNotFoundException if the product is not found.
     */
    private Mono<ProductEntity> findProductOrFail(Long productId) {
        return productRepository.findById(productId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Product with ID {} not found for stock validation", productId);
                    return Mono.error(new InventoryNotFoundException(productId));
                }));
    }

    /**
     * Checks if the product has sufficient quantity available.
     *
     * @param product           The product to check.
     * @param requestedQuantity The quantity to validate against.
     * @return Mono.empty() if sufficient, otherwise error Mono.
     * @throws InsufficientStockException if the quantity is not enough.
     */
    private Mono<Void> checkSufficientStock(ProductEntity product, int requestedQuantity) {
        if (product.getQuantity() < requestedQuantity) {
            log.warn("Stock insufficient for product ID: {}. Requested: {}, Available: {}",
                    product.getId(), requestedQuantity, product.getQuantity());
            return Mono.error(new InsufficientStockException(
                    product.getId(), requestedQuantity, product.getQuantity()
            ));
        }
        return Mono.empty();
    }

    /**
     * Publishes an InventoryUpdatedEvent to Kafka based on the product's current state.
     *
     * @param product The product for which the event will be published.
     * @return Mono emitting the original product after publishing the event.
     */
    private Mono<ProductEntity> sendUpdateEvent(ProductEntity product, int delta) {
        return categoryRepository.findById(product.getCategoryId())
                .doOnNext(category -> {
                    InventoryEventType eventType = delta > 0 ? InventoryEventType.STOCK_INCREASE
                            : InventoryEventType.STOCK_DECREASE;
                    InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getName())
                            .newQuantity(product.getQuantity())
                            .productName(product.getName())
                            .productId(product.getId())
                            .eventType(eventType)
                            .build();

                    String topic = delta > 0
                            ? environmentConfig.getTopics().getNewInventory()
                            : environmentConfig.getTopics().getInventoryUpdated();
                    kafkaPublisherService.publish(topic, String.valueOf(product.getId()), event);
                })
                .doOnError(e -> {
                    log.error("Failed to publish Kafka event for product ID: {}", product.getId(), e);
                    throw new KafkaPublishException("Error publishing Kafka event for product ID: " + product.getId(), e);
                })
                .thenReturn(product);
    }

}
