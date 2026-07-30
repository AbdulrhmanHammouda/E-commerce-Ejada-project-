package com.example.shopservice.service;

import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.shopservice.exception.ResourceNotFoundException;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    // Get products with optional filters
    public Page<Product> getProducts(String search, String audience, Boolean isBestSeller, Boolean isMostPopular, Boolean isNewArrival, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return productRepository.findByNameContainingIgnoreCase(search, pageable);
        } else if (audience != null && !audience.isEmpty()) {
            return productRepository.findByTargetAudienceIgnoreCase(audience, pageable);
        } else if (Boolean.TRUE.equals(isBestSeller)) {
            return productRepository.findByIsBestSellerTrue(pageable);
        } else if (Boolean.TRUE.equals(isMostPopular)) {
            return productRepository.findByIsMostPopularTrue(pageable);
        } else if (Boolean.TRUE.equals(isNewArrival)) {
            return productRepository.findByIsNewArrivalTrue(pageable);
        }
        
        // If no filters are provided, return everything
        return productRepository.findAll(pageable);
    }

    // Get a single product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Add a new product to the database (handles image upload)
    public Product createProduct(Product product, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            String imageUrl = imageUploadService.uploadImage(image);
            product.setImageUrl(imageUrl);
        }
        return productRepository.save(product);
    }

    // Update an existing product
    public Product updateProduct(Long id, Product updatedProduct, MultipartFile image) {
        return productRepository.findById(id).map(existingProduct -> {
            if (updatedProduct.getName() != null) existingProduct.setName(updatedProduct.getName());
            if (updatedProduct.getDescription() != null) existingProduct.setDescription(updatedProduct.getDescription());
            if (updatedProduct.getPrice() != null) existingProduct.setPrice(updatedProduct.getPrice());
            if (updatedProduct.getCategory() != null) existingProduct.setCategory(updatedProduct.getCategory());
            if (updatedProduct.getTargetAudience() != null) existingProduct.setTargetAudience(updatedProduct.getTargetAudience());
            
            // Upload new image if provided
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                existingProduct.setImageUrl(imageUrl);
            } else if (updatedProduct.getImageUrl() != null) {
                // Allow direct URL updates (fallback)
                existingProduct.setImageUrl(updatedProduct.getImageUrl());
            }
            
            return productRepository.save(existingProduct);
        }).orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }
}
