package com.example.shopservice.service;

import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // Get products with optional filters
    public List<Product> getProducts(String audience, Boolean isBestSeller, Boolean isMostPopular, Boolean isNewArrival) {
        if (audience != null && !audience.isEmpty()) {
            return productRepository.findByTargetAudienceIgnoreCase(audience);
        } else if (Boolean.TRUE.equals(isBestSeller)) {
            return productRepository.findByIsBestSellerTrue();
        } else if (Boolean.TRUE.equals(isMostPopular)) {
            return productRepository.findByIsMostPopularTrue();
        } else if (Boolean.TRUE.equals(isNewArrival)) {
            return productRepository.findByIsNewArrivalTrue();
        }
        
        // If no filters are provided, return everything
        return productRepository.findAll();
    }

    // Get a single product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Add a new product to the database
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
}
