package org.inventorysystem.storeservice.repository;

import org.inventorysystem.storeservice.entity.CategoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends ReactiveCrudRepository<CategoryEntity, Long> {
}
