package org.inventorysystem.storeservice;

import org.inventorysystem.storeservice.event.CategoryCreatedEvent;
import org.inventorysystem.storeservice.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec genericExecuteSpec;

    private CategoryService categoryService;

    @BeforeEach
    void setup() {
        categoryService = new CategoryService(databaseClient);
    }

    @Test
    void createFromEvent_shouldInsertCategory() {
        // Arrange
        CategoryCreatedEvent event = new CategoryCreatedEvent(1L, "Luxury");

        when(databaseClient.sql(anyString())).thenReturn(genericExecuteSpec);
        when(genericExecuteSpec.bind(eq("id"), any())).thenReturn(genericExecuteSpec);
        when(genericExecuteSpec.bind(eq("name"), any())).thenReturn(genericExecuteSpec);
        when(genericExecuteSpec.then()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(categoryService.createFromEvent(event))
                .verifyComplete();

        verify(databaseClient).sql("INSERT INTO categories (id, name) VALUES (:id, :name)");
        verify(genericExecuteSpec).bind("id", 1L);
        verify(genericExecuteSpec).bind("name", "Luxury");
        verify(genericExecuteSpec).then();
    }

    @Test
    void createFromEvent_shouldHandleInsertErrorGracefully() {
        // Arrange
        CategoryCreatedEvent event = new CategoryCreatedEvent(2L, "Sport");

        when(databaseClient.sql(anyString())).thenReturn(genericExecuteSpec);
        when(genericExecuteSpec.bind(anyString(), any())).thenReturn(genericExecuteSpec);
        when(genericExecuteSpec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        // Act & Assert
        StepVerifier.create(categoryService.createFromEvent(event))
                .expectError(RuntimeException.class)
                .verify();

        verify(genericExecuteSpec).then();
    }
}
