package com.example.shopservice.controller;

import com.example.shopservice.entity.Order;
import com.example.shopservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.OrderResponse;
import com.example.shopservice.mapper.DtoMapper;

@RestController
@RequestMapping("/api/shop/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse> checkout(@RequestHeader("X-User-Id") Long userId) {
        Order savedOrder = orderService.checkout(userId);
        return ResponseEntity.ok(new ApiResponse(true, "Checkout successful", DtoMapper.mapToOrderResponse(savedOrder)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        List<Order> orders = orderService.getOrderHistory(userId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(DtoMapper::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orderResponses));
    }
}
