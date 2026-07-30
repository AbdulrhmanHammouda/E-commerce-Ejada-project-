package com.example.shopservice.controller;

import com.example.shopservice.entity.Wishlist;
import com.example.shopservice.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/wishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // GET /api/shop/wishlists?userId=123
    @GetMapping
    public ResponseEntity<List<Wishlist>> getUserWishlist(@RequestParam String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }

    // POST /api/shop/wishlists?userId=123&productId=1
    @PostMapping
    public ResponseEntity<Wishlist> addToWishlist(
            @RequestParam String userId,
            @RequestParam Long productId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            Wishlist wishlist = wishlistService.addToWishlist(userId, productId);
            return ResponseEntity.ok(wishlist);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/shop/wishlists?userId=123&productId=1
    @DeleteMapping
    public ResponseEntity<Void> removeFromWishlist(
            @RequestParam String userId,
            @RequestParam Long productId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }
}
