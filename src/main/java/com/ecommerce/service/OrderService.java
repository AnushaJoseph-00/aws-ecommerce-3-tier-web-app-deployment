package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String ORDER_QUEUE = "order_queue";

    @Transactional
    public Order createOrder(Order order) {
        log.info("Creating new order for customer: {}", order.getCustomerId());

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = BigDecimal.ZERO;
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    total = total.add(item.getLineTotal());
                }
            }
            order.setTotalAmount(total);
        }

        // Wire up the back-reference so each OrderItem's order_id is populated
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getItems() != null && !savedOrder.getItems().isEmpty()) {
            for (OrderItem item : savedOrder.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        try {
            rabbitTemplate.convertAndSend(ORDER_QUEUE, "Order created: " + savedOrder.getId());
            log.info("Order message sent to RabbitMQ queue");
        } catch (Exception e) {
            log.warn("Failed to send order to RabbitMQ queue: {}", e.getMessage());
        }

        return savedOrder;
    }

    public Optional<Order> getOrderById(Integer id) {
        log.info("Fetching order with id: {}", id);
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByCustomerId(Integer customerId) {
        log.info("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, Order.OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        return orderRepository.findById(orderId)
                .map(order -> {
                    order.setStatus(newStatus);
                    order.setUpdatedAt(LocalDateTime.now());
                    Order updated = orderRepository.save(order);

                    try {
                        String message = "Order " + orderId + " status changed to " + newStatus;
                        rabbitTemplate.convertAndSend(ORDER_QUEUE, message);
                    } catch (Exception e) {
                        log.warn("Failed to send status update to RabbitMQ: {}", e.getMessage());
                    }

                    return updated;
                })
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        log.info("Cancelling order: {}", orderId);

        orderRepository.findById(orderId).ifPresent(order -> {
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    Product product = item.getProduct();
                    if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
            }

            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            try {
                rabbitTemplate.convertAndSend(ORDER_QUEUE, "Order cancelled: " + orderId);
            } catch (Exception e) {
                log.warn("Failed to send cancellation to RabbitMQ: {}", e.getMessage());
            }
        });
    }

    public long getTotalOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
}