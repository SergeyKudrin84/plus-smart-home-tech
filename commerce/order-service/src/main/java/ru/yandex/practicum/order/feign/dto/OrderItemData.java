package ru.yandex.practicum.order.feign.dto;

import java.math.BigDecimal;

public record OrderItemData(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price
) {
}
