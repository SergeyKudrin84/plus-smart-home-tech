package ru.yandex.practicum.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.inventory.dto.*;
import ru.yandex.practicum.inventory.service.InventoryService;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryDto>> findAll() {
        return ResponseEntity.ok(inventoryService.findAll());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryDto> findByProductId(
            @PathVariable Long productId) {

        return ResponseEntity.ok(inventoryService.findByProductId(productId));
    }

    @PostMapping
    public ResponseEntity<InventoryDto> create(
            @Valid @RequestBody UpdateInventoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(inventoryService.create(request));
    }

    @PutMapping
    public ResponseEntity<InventoryDto> update(
            @Valid @RequestBody UpdateInventoryRequest request) {

        return ResponseEntity.ok(inventoryService.update(request));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ReserveResponse> reserve(
            @Valid @RequestBody ReserveRequest request) {

        return ResponseEntity.ok(inventoryService.reserve(request));
    }

    @PostMapping("/release")
    public ResponseEntity<ReserveResponse> release(
            @Valid @RequestBody ReleaseRequest request
    ) {
        return ResponseEntity.ok(inventoryService.release(request));
    }
}
