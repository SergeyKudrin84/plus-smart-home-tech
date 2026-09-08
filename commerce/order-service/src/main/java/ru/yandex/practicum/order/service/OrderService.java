package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
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
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderDto create(CreateOrderRequest request) {
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

        return toDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderDto findById(Long id) {
        return orderRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Заказ с id " + id + " не найден"));
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDto> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private OrderItem createItem(
            OrderItemRequest request,
            Order order
    ) {
        OrderItem item = new OrderItem();

        item.setOrder(order);
        item.setProductId(request.productId());
        item.setProductName(request.productName());
        item.setQuantity(request.quantity());
        item.setPrice(request.price());

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