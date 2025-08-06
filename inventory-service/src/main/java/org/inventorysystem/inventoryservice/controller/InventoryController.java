package org.inventorysystem.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.inventorysystem.inventoryservice.dto.CreateCategoryRequest;
import org.inventorysystem.inventoryservice.dto.CreateProductRequest;
import org.inventorysystem.inventoryservice.dto.StockValidationRequest;
import org.inventorysystem.inventoryservice.dto.ValidateStockResponse;
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
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/category")
    public Mono<ResponseEntity<CategoryEntity>> createCategory(@RequestBody CreateCategoryRequest categoryRequest) {
        return inventoryService.createCategory(categoryRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<ProductEntity>> create(@RequestBody CreateProductRequest productRequest) {
        return inventoryService.create(productRequest)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/increase")
    public Mono<ResponseEntity<ProductEntity>> increase(@PathVariable Long id, @RequestParam int amount) {
        return inventoryService.updateQuantity(id, amount)
                .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}/decrease")
    public Mono<ResponseEntity<ProductEntity>> decrease(@PathVariable Long id, @RequestParam int amount) {
        return inventoryService.updateQuantity(id, -amount)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductEntity>> getById(@PathVariable Long id) {
        return inventoryService.getById(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/category/{id}")
    public Mono<ResponseEntity<List<ProductEntity>>> getByCategoryId(@PathVariable Long categoryId) {
        return inventoryService.getByCategoryId(categoryId)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/validate-stock")
    public Mono<ResponseEntity<ValidateStockResponse>> validateStock(@RequestBody StockValidationRequest request) {
        return inventoryService.validateStock(request.getProductId(), request.getQuantity())
                .map(ResponseEntity::ok);
    }
}
