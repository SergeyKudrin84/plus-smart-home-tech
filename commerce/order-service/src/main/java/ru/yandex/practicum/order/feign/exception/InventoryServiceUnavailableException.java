package ru.yandex.practicum.order.feign.exception;

public class InventoryServiceUnavailableException extends RuntimeException {

    private final Long productId;

    public InventoryServiceUnavailableException(
            Long productId,
            Throwable cause
    ) {
        super(
                "Inventory service недоступен для товара с id " + productId,
                cause
        );
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}