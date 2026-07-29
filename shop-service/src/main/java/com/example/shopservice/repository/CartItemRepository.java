package com.example.shopservice.repository;

import com.example.shopservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Find all items belonging to a specific user
    List<CartItem> findByUserId(Long userId);
}
