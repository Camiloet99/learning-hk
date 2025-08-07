package org.inventorysystem.inventoryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.inventoryservice.dto.request.CreateCategoryRequest;
import org.inventorysystem.inventoryservice.dto.request.CreateProductRequest;
import org.inventorysystem.inventoryservice.dto.request.StockValidationRequest;
import org.inventorysystem.inventoryservice.dto.response.ValidateStockResponse;
import org.inventorysystem.inventoryservice.entity.CategoryEntity;
import org.inventorysystem.inventoryservice.entity.ProductEntity;
import org.inventorysystem.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory", description = "Inventory management API")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Create a new category", description = "Creates a new product category")
    @ApiResponse(responseCode = "200", description = "Category created successfully")
    @PostMapping("/category")
    public Mono<ResponseEntity<CategoryEntity>> createCategory(
            @RequestBody @Parameter(description = "Category creation request") CreateCategoryRequest categoryRequest) {
        return inventoryService.createCategory(categoryRequest)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Create a new product", description = "Creates a new product in inventory")
    @ApiResponse(responseCode = "200", description = "Product created successfully")
    @PostMapping
    public Mono<ResponseEntity<ProductEntity>> create(
            @RequestBody @Parameter(description = "Product creation request") CreateProductRequest productRequest) {
        return inventoryService.create(productRequest)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Increase stock", description = "Increases the quantity of a product in inventory")
    @PutMapping("/{id}/increase")
    public Mono<ResponseEntity<ProductEntity>> increase(
            @PathVariable Long id,
            @RequestParam int amount) {
        return inventoryService.updateQuantity(id, amount)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Decrease stock", description = "Decreases the quantity of a product in inventory")
    @PutMapping("/{id}/decrease")
    public Mono<ResponseEntity<ProductEntity>> decrease(
            @PathVariable Long id,
            @RequestParam int amount) {
        return inventoryService.updateQuantity(id, -amount)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a product by its ID")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductEntity>> getById(@PathVariable Long id) {
        return inventoryService.getById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Get products by category ID", description = "Retrieves products that belong to a category")
    @GetMapping("/category/{id}")
    public Mono<ResponseEntity<List<ProductEntity>>> getByCategoryId(@PathVariable(name = "id") Long categoryId) {
        return inventoryService.getByCategoryId(categoryId)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Validate stock", description = "Checks if sufficient stock is available for a product")
    @PostMapping("/validate-stock")
    public Mono<ResponseEntity<ValidateStockResponse>> validateStock(
            @RequestBody StockValidationRequest request) {
        return inventoryService.validateStock(request.getProductId(), request.getQuantity())
                .map(ResponseEntity::ok);
    }
}

