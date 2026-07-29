package com.example.shopservice.controller;

import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/cart")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // GET /api/shop/cart -> View my cart
    @GetMapping
    public ResponseEntity<List<CartItem>> viewMyCart(@RequestHeader("X-User-Id") Long userId) {
        List<CartItem> myCart = cartItemRepository.findByUserId(userId);
        return ResponseEntity.ok(myCart);
    }

    // POST /api/shop/cart/add/{productId} -> Add item to cart
    @PostMapping("/add/{productId}")
    public ResponseEntity<String> addToCart(
            @PathVariable Long productId, 
            @RequestHeader("X-User-Id") Long userId) {

        // 1. Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        // 2. Add it to the user's cart
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProduct(product);
        item.setQuantity(1);
        
        cartItemRepository.save(item);

        return ResponseEntity.ok("Successfully added " + product.getName() + " to your cart!");
    }
}
