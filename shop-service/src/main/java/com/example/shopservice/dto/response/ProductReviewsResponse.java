package com.example.shopservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewsResponse {
    private Long productId;
    private Double averageRating;
    private int totalReviews;
    private List<ReviewResponse> reviews;
}
