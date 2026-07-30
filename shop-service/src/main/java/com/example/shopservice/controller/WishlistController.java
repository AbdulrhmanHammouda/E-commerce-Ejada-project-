package com.example.shopservice.controller;

import com.example.shopservice.entity.Wishlist;
import com.example.shopservice.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.WishlistResponse;
import com.example.shopservice.mapper.DtoMapper;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shop/wishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // GET /api/shop/wishlists
    @GetMapping
    public ResponseEntity<ApiResponse> getUserWishlist(@RequestHeader("X-User-Id") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID header is required", null));
        }
        List<Wishlist> wishlists = wishlistService.getUserWishlist(userId);
        List<WishlistResponse> responses = wishlists.stream()
                .map(DtoMapper::mapToWishlistResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(true, "Wishlist retrieved successfully", responses));
    }

    // POST /api/shop/wishlists/{productId}
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse> addToWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID header is required", null));
        }
        Wishlist wishlist = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Added to wishlist successfully", DtoMapper.mapToWishlistResponse(wishlist)));
    }

    // DELETE /api/shop/wishlists/{productId}
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse> removeFromWishlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID header is required", null));
        }
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(new ApiResponse(true, "Removed from wishlist successfully", null));
    }
}
