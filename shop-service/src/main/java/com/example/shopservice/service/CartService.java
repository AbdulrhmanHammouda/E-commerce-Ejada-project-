package com.example.shopservice.service;

import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> viewMyCart(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public String addToCart(Long userId, Long productId, int requestedQty) {
        // 1. Find the product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found!"));

        // 2. Check if the user already has this item in their cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByUserIdAndProduct_Id(userId, productId);

        if (existingItemOpt.isPresent()) {
            // 3a. If they do, just increase the quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + requestedQty);
            cartItemRepository.save(existingItem);
            return "Updated quantity for " + product.getName() + " in your cart! (New Qty: " + existingItem.getQuantity() + ")";
        } else {
            // 3b. If they don't, create a new cart item
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProduct(product);
            newItem.setQuantity(requestedQty);
            cartItemRepository.save(newItem);
            return "Successfully added " + product.getName() + " to your cart! (Qty: " + requestedQty + ")";
        }
    }
}
