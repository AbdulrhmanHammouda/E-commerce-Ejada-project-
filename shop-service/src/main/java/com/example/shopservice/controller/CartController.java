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
import com.example.shopservice.dto.request.AddToCartRequest;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.CartResponse;
import com.example.shopservice.mapper.DtoMapper;
import com.example.shopservice.mapper.DtoMapper;

@RestController
@RequestMapping("/api/shop/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/shop/cart -> View my cart
    @GetMapping
    public ResponseEntity<ApiResponse> viewMyCart(@RequestHeader("X-User-Id") Long userId) {
        List<CartItem> myCart = cartService.viewMyCart(userId);
        CartResponse cartResponse = DtoMapper.mapToCartResponse(userId, myCart);
        return ResponseEntity.ok(new ApiResponse(true, "Cart retrieved successfully", cartResponse));
    }

    // POST /api/shop/cart/add -> Add item to cart using DTO
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AddToCartRequest request) {

        String resultMessage = cartService.addToCart(userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(new ApiResponse(true, resultMessage, null));
    }
}
