package org.inventorysystem.inventoryservice.repository;

import org.inventorysystem.inventoryservice.entity.ProductEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductEntity, Long> {
    Flux<ProductEntity> findByCategoryId(Long categoryId);
}
