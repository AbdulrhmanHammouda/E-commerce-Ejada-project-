package com.example.shopservice.controller;

import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.ProductRepository;
import com.example.shopservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.shopservice.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/shop/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/shop/cart -> View my cart
    @GetMapping
    public ResponseEntity<ApiResponse> viewMyCart(@RequestHeader("X-User-Id") Long userId) {
        List<CartItem> myCart = cartService.viewMyCart(userId);
        return ResponseEntity.ok(new ApiResponse(true, "Cart retrieved successfully", myCart));
    }

    // POST /api/shop/cart/add/{productId}?qty=2 -> Add item to cart
    @PostMapping("/add/{productId}")
    public ResponseEntity<ApiResponse> addToCart(
            @PathVariable Long productId, 
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false, defaultValue = "1") int qty) {

        String resultMessage = cartService.addToCart(userId, productId, qty);
        return ResponseEntity.ok(new ApiResponse(true, resultMessage, null));
    }
}
