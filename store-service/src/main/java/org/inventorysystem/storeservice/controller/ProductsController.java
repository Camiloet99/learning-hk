package org.inventorysystem.storeservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.inventorysystem.storeservice.entity.ProductEntity;
import org.inventorysystem.storeservice.service.ProductsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Operations related to products and inventory")
public class ProductsController {

    private final ProductsService productsService;

    @Operation(
            summary = "Get product by ID",
            description = "Fetches a product and its inventory details by product ID."
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @GetMapping("/{productId}")
    public Mono<ResponseEntity<ProductEntity>> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long productId) {
        return productsService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok);
    }

    @Operation(
            summary = "Get products by category ID",
            description = "Fetches all products that belong to the specified category."
    )
    @ApiResponse(responseCode = "200", description = "List of products in the category")
    @GetMapping("/category/{categoryId}")
    public Mono<ResponseEntity<List<ProductEntity>>> getProductByCategoryId(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long categoryId) {
        return productsService.getInventoryByCategoryId(categoryId)
                .map(ResponseEntity::ok);
    }
}

