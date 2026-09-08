package ru.yandex.practicum.inventory.service;

import lombok.RequiredArgsConstructor;
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
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryDto> findAll() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryDto findByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(this::toDto)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Складская запись для товара с id "
                                        + productId + " не найдена"));
    }

    @Transactional
    public InventoryDto create(UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepository
                .findByProductId(request.productId())
                .orElseGet(Inventory::new);

        if (inventory.getId() != null) {
            updateQuantity(inventory, request.quantity());
        } else {
            inventory.setProductId(request.productId());
            inventory.setQuantity(request.quantity());
            inventory.setReservedQuantity(0);
        }

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryDto update(UpdateInventoryRequest request) {
        Inventory inventory = getInventory(request.productId());

        updateQuantity(inventory, request.quantity());

        return toDto(inventoryRepository.save(inventory));
    }

    @Transactional
    public ReserveResponse reserve(ReserveRequest request) {
        Inventory inventory = getInventory(request.productId());

        if (inventory.getAvailableQuantity() < request.quantity()) {
            throw new InsufficientStockException(
                    "Недостаточно товара с id " + request.productId()
                            + ": доступно " + inventory.getAvailableQuantity()
                            + ", запрошено " + request.quantity());
        }

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + request.quantity()
        );

        inventoryRepository.save(inventory);

        return new ReserveResponse(
                true,
                inventory.getAvailableQuantity(),
                "Товар успешно зарезервирован"
        );
    }

    private Inventory getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Складская запись для товара с id "
                                        + productId + " не найдена"));
    }

    private void updateQuantity(Inventory inventory, Integer quantity) {
        if (quantity < inventory.getReservedQuantity()) {
            throw new IllegalArgumentException(
                    "Общее количество товара не может быть меньше "
                            + "зарезервированного количества");
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