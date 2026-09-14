package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.feign.*;
import ru.yandex.practicum.order.feign.dto.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import feign.FeignException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOrchestrationService {

    private final OrderService orderService;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Начало оформления заказа: customerEmail={}", request.customerEmail());

        Map<Long, Integer> quantitiesByProduct = request.items().stream()
                .collect(Collectors.groupingBy(
                        OrderItemRequest::productId,
                        Collectors.summingInt(OrderItemRequest::quantity)
                ));

        log.info("Количество товаров для резервирования: {}", quantitiesByProduct);

        Map<Long, ProductDto> products = quantitiesByProduct.keySet().stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                this::getActiveProduct
                        ));

        Map<Long, Integer> reservedQuantities = new LinkedHashMap<>();

        try {
            quantitiesByProduct.forEach((productId, quantity) -> {
                reserveProduct(productId, quantity);
                reservedQuantities.put(productId, quantity);
            });

            OrderData orderData = new OrderData(
                    request.customerName(),
                    request.customerEmail(),
                    request.items()
                            .stream()
                            .map(item -> toOrderItemData(
                                    item,
                                    products.get(item.productId())
                            ))
                            .toList()
            );

            OrderDto order = orderService.create(orderData);

            log.info("Оформление заказа завершено: orderId={}", order.id());

            return order;

        } catch (OrderProcessingException e) {
            releaseReservations(reservedQuantities);

            throw e;

        } catch (Exception e) {
            log.error(
                    "Ошибка при оформлении заказа. Запускаем компенсацию резервов: {}",
                    e.getMessage(),
                    e
            );

            releaseReservations(reservedQuantities);

            throw e;
        }
    }

    private ProductDto getActiveProduct(Long productId) {
        log.info("Получение товара из product-service: productId={}", productId);

        try {
            ProductDto product = productClient.getProductById(productId);

            if (!Boolean.TRUE.equals(product.active())) {
                log.warn("Товар снят с продажи: productId={}", productId);

                throw new OrderProcessingException(
                        "Товар с id " + productId + " снят с продажи"
                );
            }

            return product;

        } catch (FeignException.NotFound e) {
            log.warn("Товар не найден: productId={}", productId);

            throw new OrderProcessingException(
                    "Товар с id " + productId + " не найден",
                    e
            );
        } catch (FeignException e) {
            log.error(
                    "Ошибка product-service: productId={}, status={}",
                    productId,
                    e.status(),
                    e
            );

            throw new OrderProcessingException(
                    "Не удалось получить данные товара с id " + productId,
                    e
            );
        }
    }

    private OrderItemData toOrderItemData(
            OrderItemRequest request,
            ProductDto product
    ) {
        return new OrderItemData(
                product.id(),
                product.name(),
                request.quantity(),
                product.price()
        );
    }

    private record Reservation(
            Long productId,
            Integer quantity
    ) {
    }

    private void reserveProduct(Long productId, Integer quantity) {
        log.info("Резервирование товара: productId={}, quantity={}", productId, quantity);

        try {
            inventoryClient.reserveStock(
                    new ReserveRequest(productId, quantity)
            );

        } catch (FeignException.NotFound e) {
            log.warn(
                    "Складская запись не найдена: productId={}",
                    productId
            );

            throw new OrderProcessingException(
                    "Складская запись для товара с id "
                            + productId + " не найдена",
                    e
            );

        } catch (FeignException.Conflict e) {
            log.warn(
                    "Недостаточно товара на складе: productId={}, quantity={}",
                    productId,
                    quantity
            );

            throw new OrderProcessingException(
                    "Недостаточно товара с id " + productId,
                    e
            );

        } catch (FeignException e) {
            log.error(
                    "Ошибка inventory-service: productId={}, status={}",
                    productId,
                    e.status(),
                    e
            );

            throw new OrderProcessingException(
                    "Не удалось зарезервировать товар с id " + productId,
                    e
            );
        }
    }

    private void releaseReservations(
            Map<Long, Integer> reservedQuantities
    ) {
        reservedQuantities.forEach((productId, quantity) -> {
            try {
                log.info(
                        "Снятие резерва: productId={}, quantity={}",
                        productId,
                        quantity
                );

                inventoryClient.releaseStock(
                        new ReleaseRequest(productId, quantity)
                );

            } catch (Exception e) {
                log.error(
                        "Не удалось снять резерв: productId={}, quantity={}",
                        productId,
                        quantity,
                        e
                );
            }
        });
    }
}
