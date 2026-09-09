package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> findAll() {
        List<ProductDto> products = productRepository.findByActiveTrue()
                .stream()
                .map(this::toDto)
                .toList();

        log.info("Найдено товаров: {}", products.size());

        return products;
    }

    public ProductDto findById(Long id) {
        log.info("Поиск товара: id={}", id);

        ProductDto productDto = productRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> {
                    log.warn("Товар с id {} не найден", id);

                    return new NotFoundException(
                            "Товар с id " + id + " не найден"
                    );
                });

        log.info("Товар найден: {}", productDto);

        return productDto;
    }

    public List<ProductDto> findByCategory(Long categoryId) {
        log.info("Поиск товаров по категории: categoryId={}", categoryId);

        List<ProductDto> products = productRepository
                .findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::toDto)
                .toList();

        log.info(
                "Для категории {} найдено активных товаров: {}",
                categoryId,
                products.size()
        );

        return products;
    }

    public List<ProductDto> search(String query) {
        log.info("Поиск товаров по запросу: '{}'", query);

        List<ProductDto> products = productRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(query)
                .stream()
                .map(this::toDto)
                .toList();

        log.info("По запросу найдено товаров: {}", products.size());

        return products;
    }

    public ProductDto create(CreateProductRequest request) {
        log.info("Создание товара: {}", request);

        Category category = getCategory(request.categoryId());

        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .imageUrl(request.imageUrl())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        log.info("Товар создан: {}", savedProduct);

        return toDto(savedProduct);
    }

    @Transactional
    public ProductDto update(Long id, UpdateProductRequest request) {
        log.info("Обновление товара: id={}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Товар с id {} не найден для обновления", id);

                    return new NotFoundException(
                            "Товар с id " + id + " не найден"
                    );
                });

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

        Product savedProduct = productRepository.save(product);

        log.info("Товар обновлён: {}", savedProduct);

        return toDto(savedProduct);
    }

    private Category getCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Категория с id {} не найдена", categoryId);

                    return new NotFoundException(
                            "Категория с id " + categoryId + " не найдена"
                    );
                });
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