package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.feign.*;
import ru.yandex.practicum.order.feign.dto.*;

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

        quantitiesByProduct.forEach((productId, quantity) -> {
            log.info("Резервирование товара: productId={}, quantity={}", productId, quantity);
            inventoryClient.reserveStock(new ReserveRequest(productId, quantity));
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

        log.info(
                "Оформление заказа завершено: orderId={}",
                order.id()
        );

        return order;
    }

    private ProductDto getActiveProduct(Long productId) {
        log.info(
                "Получение товара из product-service: productId={}",
                productId
        );

        ProductDto product = productClient.getProductById(productId);

        if (!Boolean.TRUE.equals(product.active())) {
            log.warn(
                    "Товар неактивен: productId={}",
                    productId
            );

            throw new IllegalArgumentException(
                    "Товар с id " + productId + " неактивен"
            );
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
}
