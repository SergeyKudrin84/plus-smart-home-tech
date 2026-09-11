package ru.yandex.practicum.order.feing.dto;

public record ReserveRequest(
        Long productId,
        Integer quantity
) {
}
