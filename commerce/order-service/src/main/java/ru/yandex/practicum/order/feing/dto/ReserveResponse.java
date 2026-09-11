package ru.yandex.practicum.order.feing.dto;

public record ReserveResponse(
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
