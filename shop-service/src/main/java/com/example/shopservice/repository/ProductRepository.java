package com.example.shopservice.repository;

import com.example.shopservice.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(String category, Pageable pageable);
    
    // UI Filters
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByTargetAudienceIgnoreCase(String targetAudience, Pageable pageable);
    Page<Product> findByIsBestSeller(Boolean isBestSeller, Pageable pageable);
    Page<Product> findByIsMostPopular(Boolean isMostPopular, Pageable pageable);
    Page<Product> findByIsNewArrival(Boolean isNewArrival, Pageable pageable);
    
    // Uniqueness checks
    boolean existsByNameIgnoreCase(String name);
}
