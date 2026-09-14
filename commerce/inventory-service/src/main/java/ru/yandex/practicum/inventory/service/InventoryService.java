package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.entity.Inventory;
import ru.yandex.practicum.inventory.exception.InsufficientStockException;
import ru.yandex.practicum.inventory.exception.NotFoundException;
import ru.yandex.practicum.inventory.repository.InventoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryDto> findAll() {
        List<InventoryDto> inventoryDtos = inventoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
        log.info("Найдено записей об остатках: {}", inventoryDtos.size());
        return inventoryDtos;
    }

    @Transactional(readOnly = true)
    public InventoryDto findByProductId(Long productId) {
        log.info("Поиск складской записи для товара с id: {}", productId);
        InventoryDto inventoryDto = inventoryRepository.findByProductId(productId)
                .map(this::toDto)
                .orElseThrow(() -> {
                    log.warn("Складская запись для товара с id {} не найдена", productId);

                    return new NotFoundException(
                            "Складская запись для товара с id "
                                    + productId + " не найдена"
                    );
                });

        log.info("Найдена складская запись{}", inventoryDto);
        return inventoryDto;
    }

    @Transactional
    public InventoryDto create(UpdateInventoryRequest request) {
        log.info("Создание складской записи: {}", request);
        if (inventoryRepository.findByProductId(request.productId()).isPresent()) {
            log.warn(
                    "Складская запись для товара с id {} уже существует",
                    request.productId()
            );

            throw new InsufficientStockException(
                    "Запись об остатках для товара с id "
                            + request.productId() + " уже существует"
            );
        }

        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Складская запись создана: {}", savedInventory);

        return toDto(savedInventory);
    }

    @Transactional
    public InventoryDto update(UpdateInventoryRequest request) {
        log.info("Обновление складской записи: {}", request);
        Inventory inventory = getInventory(request.productId());

        updateQuantity(inventory, request.quantity());

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Складская запись обновлена: {}", savedInventory);

        return toDto(savedInventory);
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        log.info("Резервирование товара: {}", request);

        Inventory inventory = getInventory(request.productId());

        Integer availableQuantity = inventory.getAvailableQuantity();

        log.debug(
                "Доступное количество товара: productId={}, availableQuantity={}",
                request.productId(),
                availableQuantity
        );

        if (availableQuantity < request.quantity()) {
            log.warn(
                    "Недостаточно товара для резервирования: productId={}, availableQuantity={}, requestedQuantity={}",
                    request.productId(),
                    availableQuantity,
                    request.quantity()
            );

            throw new InsufficientStockException(
                    "Недостаточно товара с id " + request.productId()
                            + ": доступно " + availableQuantity
                            + ", запрошено " + request.quantity()
            );
        }

        Integer oldReservedQuantity = inventory.getReservedQuantity();

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + request.quantity()
        );

        inventoryRepository.save(inventory);

        log.info(
                "Товар успешно зарезервирован: productId={}, reservedQuantity: {} -> {}, availableQuantity={}",
                request.productId(),
                oldReservedQuantity,
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );

        return new ReserveResponse(
                true,
                inventory.getAvailableQuantity(),
                "Товар успешно зарезервирован"
        );
    }

    private Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Складская запись для товара с id {} не найдена", productId);

                    return new NotFoundException(
                            "Складская запись для товара с id "
                                    + productId + " не найдена"
                    );
                });
    }

    private void updateQuantity(Inventory inventory, Integer quantity) {
        if (quantity < inventory.getReservedQuantity()) {
            log.warn(
                    "Невозможно обновить количество: productId={}, quantity={}, reservedQuantity={}",
                    inventory.getProductId(),
                    quantity,
                    inventory.getReservedQuantity()
            );

            throw new IllegalArgumentException(
                    "Общее количество товара не может быть меньше "
                            + "зарезервированного количества"
            );
        }

        inventory.setQuantity(quantity);
    }

    private InventoryDto toDto(Inventory inventory) {
        return new InventoryDto(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}