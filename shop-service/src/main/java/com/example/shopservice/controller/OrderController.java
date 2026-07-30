package com.example.shopservice.controller;

import com.example.shopservice.entity.Order;
import com.example.shopservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.OrderResponse;
import com.example.shopservice.dto.response.OrderItemResponse;

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
        return ResponseEntity.ok(new ApiResponse(true, "Checkout successful", mapToOrderResponse(savedOrder)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getMyOrders(@RequestHeader("X-User-Id") Long userId) {
        List<Order> orders = orderService.getOrderHistory(userId);
        List<OrderResponse> orderResponses = orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(true, "Orders retrieved successfully", orderResponses));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse res = new OrderItemResponse();
            res.setId(item.getId());
            res.setProductId(item.getProduct().getId());
            res.setProductName(item.getProduct().getName());
            res.setQuantity(item.getQuantity());
            res.setPrice(item.getPrice());
            res.setSubTotal(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
            return res;
        }).collect(Collectors.toList());

        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setUserId(order.getUserId());
        res.setTotalPrice(order.getTotalAmount());
        res.setStatus(order.getStatus().name());
        res.setCreatedAt(order.getCreatedAt());
        res.setItems(itemResponses);
        return res;
    }
}
