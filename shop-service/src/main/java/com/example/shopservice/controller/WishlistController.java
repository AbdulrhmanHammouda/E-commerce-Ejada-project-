package com.example.shopservice.controller;

import com.example.shopservice.entity.Wishlist;
import com.example.shopservice.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.shopservice.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/shop/wishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // GET /api/shop/wishlists?userId=123
    @GetMapping
    public ResponseEntity<ApiResponse> getUserWishlist(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID is required", null));
        }
        return ResponseEntity.ok(new ApiResponse(true, "Wishlist retrieved successfully", wishlistService.getUserWishlist(userId)));
    }

    // POST /api/shop/wishlists?userId=123&productId=1
    @PostMapping
    public ResponseEntity<ApiResponse> addToWishlist(
            @RequestParam String userId,
            @RequestParam Long productId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID is required", null));
        }
        try {
            Wishlist wishlist = wishlistService.addToWishlist(userId, productId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Added to wishlist successfully", wishlist));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // DELETE /api/shop/wishlists?userId=123&productId=1
    @DeleteMapping
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @RequestParam String userId,
            @RequestParam Long productId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID is required", null));
        }
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(new ApiResponse(true, "Removed from wishlist successfully", null));
    }
}
