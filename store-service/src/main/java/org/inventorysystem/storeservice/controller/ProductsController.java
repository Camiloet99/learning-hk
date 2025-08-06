package org.inventorysystem.storeservice.controller;

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
public class ProductsController {

    private final ProductsService productsService;

    @GetMapping("/{productId}")
    public Mono<ResponseEntity<ProductEntity>> getProductById(@PathVariable Long productId) {
        return productsService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/category/{categoryId}")
    public Mono<ResponseEntity<List<ProductEntity>>> getProductByCategoryId(@PathVariable Long categoryId) {
        return productsService.getInventoryByCategoryId(categoryId)
                .map(ResponseEntity::ok);
    }

}
