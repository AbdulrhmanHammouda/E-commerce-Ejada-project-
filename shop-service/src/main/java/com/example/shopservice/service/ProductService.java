package com.example.shopservice.service;

import com.example.shopservice.entity.Product;
import com.example.shopservice.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageUploadService imageUploadService;

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
            if (updatedProduct.getStockQuantity() != null) existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
            if (updatedProduct.getTargetAudience() != null) existingProduct.setTargetAudience(updatedProduct.getTargetAudience());
            if (updatedProduct.getIsBestSeller() != null) existingProduct.setIsBestSeller(updatedProduct.getIsBestSeller());
            if (updatedProduct.getIsMostPopular() != null) existingProduct.setIsMostPopular(updatedProduct.getIsMostPopular());
            if (updatedProduct.getIsNewArrival() != null) existingProduct.setIsNewArrival(updatedProduct.getIsNewArrival());
            
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
