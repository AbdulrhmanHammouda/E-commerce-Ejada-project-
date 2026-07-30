package com.example.shopservice.controller;

import com.example.shopservice.entity.Wishlist;
import com.example.shopservice.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.WishlistResponse;
import com.example.shopservice.dto.response.ProductResponse;
import com.example.shopservice.entity.Product;

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
        List<Wishlist> wishlists = wishlistService.getUserWishlist(userId);
        List<WishlistResponse> responses = wishlists.stream()
                .map(this::mapToWishlistResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse(true, "Wishlist retrieved successfully", responses));
    }

    // POST /api/shop/wishlists?userId=123&productId=1
    @PostMapping
    public ResponseEntity<ApiResponse> addToWishlist(
            @RequestParam String userId,
            @RequestParam Long productId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "User ID is required", null));
        }
        Wishlist wishlist = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Added to wishlist successfully", mapToWishlistResponse(wishlist)));
    }

    private WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        WishlistResponse res = new WishlistResponse();
        res.setId(wishlist.getId());
        res.setUserId(wishlist.getUserId());
        Product p = wishlist.getProduct();
        if (p != null) {
            res.setProduct(new ProductResponse(
                    p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getCategory(), p.getImageUrl(), p.getTargetAudience(), p.isBestSeller(), p.isMostPopular(), p.isNewArrival()
            ));
        }
        return res;
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
