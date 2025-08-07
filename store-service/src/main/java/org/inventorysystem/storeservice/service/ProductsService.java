package org.inventorysystem.storeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.exception.StoreInventoryNotFoundException;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service layer that handles product inventory retrieval operations
 * for a specific store. Interacts with the StoreInventoryRepository
 * to fetch product data based on product ID or category ID.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsService {

    private final StoreInventoryRepository storeInventoryRepository;

    /**
     * Retrieves a product from the store inventory by its ID.
     *
     * @param productId the ID of the product to retrieve
     * @return Mono emitting the ProductEntity if found, or error if not found
     */
    public Mono<ProductEntity> getInventoryByProductId(Long productId) {
        return storeInventoryRepository.findById(productId)
                .switchIfEmpty(Mono.error(new StoreInventoryNotFoundException(productId)))
                .doOnSuccess(item -> log.info("Fetched inventory for productId={}", productId));
    }

    /**
     * Retrieves all products from the store inventory by category ID.
     *
     * @param categoryId the category ID of the products to retrieve
     * @return Mono emitting a list of ProductEntity instances or error if none found
     */
    public Mono<List<ProductEntity>> getInventoryByCategoryId(Long categoryId) {
        return storeInventoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new StoreInventoryNotFoundException(categoryId)))
                .collectList();
    }

}
