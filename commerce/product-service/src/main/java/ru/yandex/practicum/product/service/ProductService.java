package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> findAll() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ProductDto findById(Long id) {
        return productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new NotFoundException("Товар с id " + id + " не найден"));
    }

    public List<ProductDto> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<ProductDto> search(String query) {
        return productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public ProductDto create(CreateProductRequest request) {
        Category category = getCategory(request.categoryId());

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setImageUrl(request.imageUrl());
        product.setActive(true);

        return toDto(productRepository.save(product));
    }

    public ProductDto update(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Товар с id " + id + " не найден"));

        if (request.name() != null) {
            product.setName(request.name());
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.categoryId() != null) {
            product.setCategory(getCategory(request.categoryId()));
        }

        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        if (request.active() != null) {
            product.setActive(request.active());
        }

        return toDto(productRepository.save(product));
    }

    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Категория с id " + categoryId + " не найдена"));
    }

    private ProductDto toDto(Product product) {
        Category category = product.getCategory();

        CategoryDto categoryDto = new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                categoryDto,
                product.getImageUrl(),
                product.getActive()
        );
    }
}