package org.inventorysystem.orderservice.repository;

import org.inventorysystem.orderservice.entity.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {
    Flux<OrderEntity> findByUserId(Long userId);
    Flux<OrderEntity> findByStoreId(Long storeId);
}
