package com.example.walletservice.controller;

import com.example.walletservice.dto.response.ApiResponse;
import com.example.walletservice.entity.User;
import com.example.walletservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserSummaryDto> summaries = users.stream()
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole() != null ? u.getRole().name() : "USER"
                ))
                .toList();
        return ResponseEntity.ok(new ApiResponse(true, "All users retrieved successfully", summaries));
    }

    public record UserSummaryDto(Long id, String fullName, String email, String role) {}
}
