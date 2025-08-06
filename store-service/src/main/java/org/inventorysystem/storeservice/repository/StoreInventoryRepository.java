package org.inventorysystem.storeservice.repository;

import org.inventorysystem.storeservice.entity.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StoreInventoryRepository extends ReactiveCrudRepository<ProductEntity, Long> {
    Flux<ProductEntity> findByCategoryId(Long categoryId);
}
