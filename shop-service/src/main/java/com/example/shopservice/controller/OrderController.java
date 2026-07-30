package com.example.shopservice.controller;

import com.example.shopservice.entity.Order;
import com.example.shopservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestHeader("X-User-Id") Long userId) {
        Order savedOrder = orderService.checkout(userId);
        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }
}
