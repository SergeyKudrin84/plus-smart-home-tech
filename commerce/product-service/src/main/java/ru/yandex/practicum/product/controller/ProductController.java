package ru.yandex.practicum.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductDto> findAll() {
        return productService.findAll();
    }

    @GetMapping("/search")
    public List<ProductDto> search(@RequestParam String query) {
        return productService.search(query);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductDto> findByCategory(
            @PathVariable Long categoryId) {

        return productService.findByCategory(categoryId);
    }

    @GetMapping("/{id}")
    public ProductDto findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(
            @Valid @RequestBody CreateProductRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    @PatchMapping("/{id}")
    public ProductDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        return productService.update(id, request);
    }
}