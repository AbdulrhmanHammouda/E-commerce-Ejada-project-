package com.example.shopservice.controller;

import com.example.shopservice.entity.Review;
import com.example.shopservice.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shop/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // GET /api/shop/reviews?productId=1
    @GetMapping
    public ResponseEntity<List<Review>> getProductReviews(@RequestParam Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    // POST /api/shop/reviews?productId=1
    @PostMapping
    public ResponseEntity<Review> addReview(
            @RequestParam Long productId,
            @RequestBody Review review) {
        try {
            Review savedReview = reviewService.addReview(productId, review);
            return ResponseEntity.ok(savedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
