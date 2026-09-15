package ru.yandex.practicum.order.feign;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.feign.dto.ReleaseRequest;
import ru.yandex.practicum.order.feign.dto.ReserveRequest;
import ru.yandex.practicum.order.feign.dto.ReserveResponse;
import ru.yandex.practicum.order.feign.exception.InventoryServiceUnavailableException;

@Component
@Slf4j
public class InventoryClientFallbackFactory
        implements FallbackFactory<InventoryClient> {

    @Override
    public InventoryClient create(Throwable cause) {
        log.error(
                "Ошибка вызова inventory-service. Причина: {}",
                cause.getMessage(),
                cause
        );

        return new InventoryClient() {

            @Override
            public ReserveResponse reserveStock(
                    ReserveRequest request
            ) {

                if (isBusinessFailure(cause)) {
                    throw (FeignException) cause;
                }

                throw new InventoryServiceUnavailableException(
                        request.productId(),
                        cause
                );
            }

            @Override
            public ReserveResponse releaseStock(
                    ReleaseRequest request
            ) {

                if (isBusinessFailure(cause)) {
                    throw (FeignException) cause;
                }

                throw new InventoryServiceUnavailableException(
                        request.productId(),
                        cause
                );
            }
        };
    }

    private boolean isBusinessFailure(Throwable cause) {
        return cause instanceof FeignException.NotFound
                || cause instanceof FeignException.Conflict;
    }
}
