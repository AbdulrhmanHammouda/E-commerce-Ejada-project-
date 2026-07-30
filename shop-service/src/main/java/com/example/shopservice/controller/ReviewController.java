package com.example.shopservice.controller;

import com.example.shopservice.entity.Review;
import com.example.shopservice.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import com.example.shopservice.dto.response.ApiResponse;

@RestController
@RequestMapping("/api/shop/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // GET /api/shop/reviews?productId=1
    @GetMapping
    public ResponseEntity<ApiResponse> getProductReviews(@RequestParam Long productId) {
        return ResponseEntity.ok(new ApiResponse(true, "Reviews retrieved successfully", reviewService.getProductReviews(productId)));
    }

    // POST /api/shop/reviews?productId=1
    @PostMapping
    public ResponseEntity<ApiResponse> addReview(
            @RequestParam Long productId,
            @RequestBody Review review) {
        Review savedReview = reviewService.addReview(productId, review);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Review added successfully", savedReview));
    }
}
