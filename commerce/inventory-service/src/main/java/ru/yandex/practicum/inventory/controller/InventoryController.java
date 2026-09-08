package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.InventoryDto;
import ru.yandex.practicum.inventory.dto.ReserveRequest;
import ru.yandex.practicum.inventory.dto.ReserveResponse;
import ru.yandex.practicum.inventory.dto.UpdateInventoryRequest;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryDto> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/{productId}")
    public InventoryDto findByProductId(
            @PathVariable Long productId) {

        return inventoryService.findByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<InventoryDto> create(
            @Valid @RequestBody UpdateInventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.create(request));
    }

    @PutMapping
    public InventoryDto update(
            @Valid @RequestBody UpdateInventoryRequest request) {

        return inventoryService.update(request);
    }

    @PostMapping("/reserve")
    public ReserveResponse reserve(
            @Valid @RequestBody ReserveRequest request) {

        return inventoryService.reserve(request);
    }
}
