package com.example.shopservice.repository;

import com.example.shopservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);
    
    // UI Filters
    List<Product> findByTargetAudienceIgnoreCase(String targetAudience);
    List<Product> findByIsBestSellerTrue();
    List<Product> findByIsMostPopularTrue();
    List<Product> findByIsNewArrivalTrue();
}
