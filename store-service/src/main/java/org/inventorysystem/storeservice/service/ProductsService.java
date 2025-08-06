package org.inventorysystem.storeservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.exception.StoreInventoryNotFoundException;
import org.inventorysystem.storeservice.repository.StoreInventoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsService {

    private final StoreInventoryRepository storeInventoryRepository;

    public Mono<ProductEntity> getInventoryByProductId(Long productId) {
        return storeInventoryRepository.findById(productId)
                .switchIfEmpty(Mono.error(new StoreInventoryNotFoundException(productId)))
                .doOnSuccess(item -> log.info("Fetched inventory for productId={}", productId));
    }

    public Mono<List<ProductEntity>> getInventoryByCategoryId(Long categoryId) {
        return storeInventoryRepository.findByCategoryId(categoryId)
                .switchIfEmpty(Mono.error(new StoreInventoryNotFoundException(categoryId)))
                .collectList();
    }

}
