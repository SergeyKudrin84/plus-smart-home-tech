package ru.yandex.practicum.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> findAll() {
        List<CategoryDto> categories = categoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();

        log.info("Найдено категорий: {}", categories.size());

        return categories;
    }

    public CategoryDto findById(Long id) {
        log.info("Поиск категории: id={}", id);

        CategoryDto categoryDto = categoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> {
                    log.warn("Категория с id {} не найдена", id);

                    return new NotFoundException(
                            "Категория с id " + id + " не найдена"
                    );
                });

        log.info("Категория найдена: {}", categoryDto.id());

        return categoryDto;
    }

    public CategoryDto create(CreateCategoryRequest request) {
        log.info("Создание категории: {}", request);
        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category savedCategory = categoryRepository.save(category);

        log.info("Категория создана: {}", savedCategory);

        return toDto(savedCategory);
    }

    private CategoryDto toDto(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}