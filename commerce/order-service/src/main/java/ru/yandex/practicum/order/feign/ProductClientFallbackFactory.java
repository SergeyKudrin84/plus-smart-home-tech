package ru.yandex.practicum.order.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.feign.dto.ProductDto;
import ru.yandex.practicum.order.feign.exception.ProductServiceUnavailableException;

@Component
@Slf4j
public class ProductClientFallbackFactory
        implements FallbackFactory<ProductClient> {

    @Override
    public ProductClient create(Throwable cause) {
        log.error(
                "Ошибка вызова product-service. Причина: {}",
                cause.getMessage(),
                cause
        );

        return new ProductClient() {

            @Override
            public ProductDto getProductById(Long productId) {

                Throwable actualCause = unwrap(cause);

                if (actualCause instanceof FeignException feignException) {
                    throw feignException;
                }

                throw new ProductServiceUnavailableException(
                        productId,
                        cause
                );
            }
        };
    }

    private Throwable unwrap(Throwable cause) {
        Throwable current = cause;

        while (current.getCause() != null
                && !(current instanceof FeignException)) {
            current = current.getCause();
        }

        return current;
    }
}