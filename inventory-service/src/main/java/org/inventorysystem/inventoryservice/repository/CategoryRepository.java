package org.inventorysystem.inventoryservice.repository;

import org.inventorysystem.inventoryservice.entity.CategoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ReactiveCrudRepository<CategoryEntity, Long> {
}
