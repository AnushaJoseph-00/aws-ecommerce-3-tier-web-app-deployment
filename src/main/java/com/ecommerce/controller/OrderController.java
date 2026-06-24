package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        log.info("POST /api/v1/orders - Creating new order for customer: {}", order.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Integer id) {
        log.info("GET /api/v1/orders/{} - Fetching order", id);
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Integer customerId) {
        log.info("GET /api/v1/orders/customer/{} - Fetching orders for customer", customerId);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        log.info("GET /api/v1/orders/status/{} - Fetching orders with status", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Integer id, @RequestBody OrderStatusUpdate statusUpdate) {
        log.info("PUT /api/v1/orders/{}/status - Updating order status to {}", id, statusUpdate.status);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, statusUpdate.status));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Integer id) {
        log.info("DELETE /api/v1/orders/{}/cancel - Cancelling order", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}/count")
    public ResponseEntity<Long> getTotalOrdersByStatus(@PathVariable Order.OrderStatus status) {
        log.info("GET /api/v1/orders/status/{}/count - Getting count of orders with status", status);
        return ResponseEntity.ok(orderService.getTotalOrdersByStatus(status));
    }

    @Data
    public static class OrderStatusUpdate {
        public Order.OrderStatus status;
    }
}