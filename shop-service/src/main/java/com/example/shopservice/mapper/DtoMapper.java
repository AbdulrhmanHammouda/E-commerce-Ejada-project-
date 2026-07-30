package com.example.shopservice.mapper;

import com.example.shopservice.dto.response.*;
import com.example.shopservice.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static ProductResponse mapToProductResponse(Product product) {
        if (product == null) return null;
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getImageUrl(),
                product.getTargetAudience(),
                product.isBestSeller(),
                product.isMostPopular(),
                product.isNewArrival()
        );
    }

    public static CartResponse mapToCartResponse(Long userId, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            CartItemResponse res = new CartItemResponse();
            res.setId(item.getId());
            res.setProductId(item.getProduct().getId());
            res.setProductName(item.getProduct().getName());
            res.setProductPrice(item.getProduct().getPrice());
            res.setImageUrl(item.getProduct().getImageUrl());
            res.setQuantity(item.getQuantity());
            res.setSubTotal(item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())));
            return res;
        }).collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse cartResponse = new CartResponse();
        cartResponse.setUserId(userId);
        cartResponse.setItems(itemResponses);
        cartResponse.setTotalPrice(total);
        return cartResponse;
    }

    public static OrderResponse mapToOrderResponse(Order order) {
        if (order == null) return null;
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream().map(item -> {
            OrderItemResponse res = new OrderItemResponse();
            res.setId(item.getId());
            res.setProductId(item.getProduct().getId());
            res.setProductName(item.getProduct().getName());
            res.setQuantity(item.getQuantity());
            res.setPrice(item.getPriceAtPurchase());
            res.setSubTotal(item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())));
            return res;
        }).collect(Collectors.toList());

        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setUserId(order.getUserId());
        res.setTotalPrice(order.getTotalAmount());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());
        res.setItems(itemResponses);
        return res;
    }

    public static WishlistResponse mapToWishlistResponse(Wishlist wishlist) {
        if (wishlist == null) return null;
        WishlistResponse res = new WishlistResponse();
        res.setId(wishlist.getId());
        res.setUserId(wishlist.getUserId());
        res.setProduct(mapToProductResponse(wishlist.getProduct()));
        return res;
    }

    public static ReviewResponse mapToReviewResponse(Review review) {
        if (review == null) return null;
        ReviewResponse res = new ReviewResponse();
        res.setId(review.getId());
        res.setCustomerName(review.getCustomerName());
        res.setRating(review.getRating());
        res.setReviewText(review.getReviewText());
        res.setProductId(review.getProduct().getId());
        res.setCreatedAt(review.getCreatedAt());
        return res;
    }

    public static ProductReviewsResponse mapToProductReviewsResponse(Long productId, List<Review> reviews) {
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(DtoMapper::mapToReviewResponse)
                .collect(Collectors.toList());

        double average = reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);

        // Round to 1 decimal place
        average = Math.round(average * 10.0) / 10.0;

        ProductReviewsResponse res = new ProductReviewsResponse();
        res.setProductId(productId);
        res.setReviews(reviewResponses);
        res.setTotalReviews(reviews.size());
        res.setAverageRating(average);
        return res;
    }
}
