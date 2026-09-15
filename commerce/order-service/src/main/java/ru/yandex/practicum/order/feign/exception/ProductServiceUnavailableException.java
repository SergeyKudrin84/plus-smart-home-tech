package ru.yandex.practicum.order.feign.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    private final Long productId;

    public ProductServiceUnavailableException(
            Long productId,
            Throwable cause
    ) {
        super(
                "Product service недоступен для товара с id " + productId,
                cause
        );
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;

    }
}