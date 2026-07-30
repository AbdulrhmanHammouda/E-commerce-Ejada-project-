package com.example.shopservice.service;

import com.example.shopservice.entity.Product;
import com.example.shopservice.entity.Review;
import com.example.shopservice.repository.ProductRepository;
import com.example.shopservice.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Review> getProductReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public Review addReview(Long productId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        review.setProduct(product);
        return reviewRepository.save(review);
    }
}
