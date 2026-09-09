package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.dto.OrderItemRequest;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.entity.OrderItem;
import ru.yandex.practicum.order.entity.OrderStatus;
import ru.yandex.practicum.order.exception.NotFoundException;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderDto create(CreateOrderRequest request) {
        log.info("Создание заказа: {}", request);
        Order order = new Order();

        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.CREATED);
        order.setStatusDetails(null);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = request.items().stream()
                .map(itemRequest -> createItem(itemRequest, order))
                .toList();

        order.setItems(items);

        BigDecimal totalPrice = items.stream()
                .map(item -> item.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(totalPrice);
        Order savedOrder = orderRepository.save(order);

        log.info("Заказ создан: {}", savedOrder.getId());

        return toDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        log.info("Поиск заказа: id={}", id);
        OrderDto orderDto = orderRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> {
                    log.warn("Заказ с id {} не найден", id);

                    return new NotFoundException(
                            "Заказ с id " + id + " не найден"
                    );
                });

        log.info("Заказ найден: {}", orderDto.id());

        return orderDto;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        List<OrderDto> orders = orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();

        log.info("Найдено заказов: {}", orders.size());

        return orders;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByCustomerEmail(String email) {
        log.info("Поиск заказов по email клиента: {}", email);

        List<OrderDto> orders = orderRepository.findByCustomerEmail(email)
                .stream()
                .map(this::toDto)
                .toList();

        log.info(
                "По email {} найдено заказов: {}",
                email,
                orders.size()
        );

        return orders;
    }

    private OrderItem createItem(
            OrderItemRequest request,
            Order order
    ) {

        log.debug("Добавление товара в заказ: {}", request);

        OrderItem item = OrderItem.builder()
                .order(order)
                .productId(request.productId())
                .productName(request.productName())
                .quantity(request.quantity())
                .price(request.price())
                .build();

        return item;
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> new OrderItemDto(
                        item.getId(),
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .toList();

        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getStatusDetails(),
                order.getCreatedAt(),
                items
        );
    }
}