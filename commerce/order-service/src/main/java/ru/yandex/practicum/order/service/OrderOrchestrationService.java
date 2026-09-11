package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.feign.*;
import ru.yandex.practicum.order.feign.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        List<Reservation> reservations = new ArrayList<>();

        try {
            quantitiesByProduct.forEach((productId, quantity) -> {
                reserveProduct(productId, quantity);
                reservations.add(new Reservation(productId, quantity));
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

        } catch (Exception e) {
            log.error(
                    "Ошибка при оформлении заказа. Запускаем компенсацию резервов: {}",
                    e.getMessage(),
                    e
            );

            releaseReservations(reservations);

            throw e;
        }
    }

    private ProductDto getActiveProduct(Long productId) {
        log.info("Получение товара из product-service: productId={}", productId);

        ProductDto product = productClient.getProductById(productId);

        if (!Boolean.TRUE.equals(product.active())) {
            log.warn("Товар неактивен: productId={}", productId);

            throw new IllegalArgumentException("Товар с id " + productId + " неактивен");
        }

        return product;
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

        inventoryClient.reserveStock(new ReserveRequest(productId, quantity));

        log.info("Резервирование успешно: productId={}, quantity={}", productId, quantity);
    }

    private void releaseReservations(
            List<Reservation> reservations
    ) {
        for (Reservation reservation : reservations) {
            try {
                log.info(
                        "Снятие резерва: productId={}, quantity={}",
                        reservation.productId(),
                        reservation.quantity()
                );

                inventoryClient.releaseStock(
                        new ReleaseRequest(
                                reservation.productId(),
                                reservation.quantity()
                        )
                );

                log.info(
                        "Резерв снят: productId={}, quantity={}",
                        reservation.productId(),
                        reservation.quantity()
                );

            } catch (Exception e) {
                log.error(
                        "Не удалось снять резерв: productId={}, quantity={}",
                        reservation.productId(),
                        reservation.quantity(),
                        e
                );
            }
        }
    }
}
