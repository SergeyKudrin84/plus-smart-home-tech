package ru.yandex.practicum.order.feign.dto;

public record ReserveResponse(
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
