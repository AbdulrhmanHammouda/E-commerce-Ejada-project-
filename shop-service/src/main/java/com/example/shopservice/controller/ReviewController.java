package com.example.shopservice.controller;

import com.example.shopservice.entity.Review;
import com.example.shopservice.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.example.shopservice.dto.response.ApiResponse;
import com.example.shopservice.dto.response.ReviewResponse;
import com.example.shopservice.mapper.DtoMapper;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

import com.example.shopservice.dto.response.ProductReviewsResponse;

@RestController
@RequestMapping("/api/shop/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // GET /api/shop/reviews/{productId}
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getProductReviews(productId);
        ProductReviewsResponse response = DtoMapper.mapToProductReviewsResponse(productId, reviews);
        return ResponseEntity.ok(new ApiResponse(true, "Reviews retrieved successfully", response));
    }

    // POST /api/shop/reviews/{productId}
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse> addReview(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId,
            @RequestBody Review review) {
        Review savedReview = reviewService.addReview(userId, productId, review);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "Review added successfully", DtoMapper.mapToReviewResponse(savedReview)));
    }
}
