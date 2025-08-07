package org.inventorysystem.storeservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.storeservice.event.CategoryCreatedEvent;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service responsible for handling category-related operations.
 * Specifically, persists new categories received from Kafka events.
 */
@Slf4j
@Service
@AllArgsConstructor
public class CategoryService {

    private final DatabaseClient databaseClient;

    /**
     * Persists a new category into the database based on a Kafka event.
     *
     * @param event The CategoryCreatedEvent containing the category ID and name.
     * @return Mono<Void> indicating completion of the insertion.
     */
    public Mono<Void> createFromEvent(CategoryCreatedEvent event) {
        return databaseClient.sql("INSERT INTO categories (id, name) VALUES (:id, :name)")
                .bind("id", event.getCategoryId())
                .bind("name", event.getCategoryName())
                .then()
                .doOnSuccess(unused -> log.info("Category inserted with ID: " + event.getCategoryId()))
                .doOnError(e -> log.error("Failed to insert category: " + e.getMessage()));
    }
}

