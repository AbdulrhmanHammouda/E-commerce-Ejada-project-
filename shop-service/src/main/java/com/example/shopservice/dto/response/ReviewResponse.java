package com.example.shopservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String customerName;
    private Integer rating;
    private String reviewText;
    private Long productId;
    private LocalDateTime createdAt;
}
