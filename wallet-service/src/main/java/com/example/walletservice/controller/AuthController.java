package com.example.walletservice.controller;

import com.example.walletservice.dto.request.LoginRequest;
import com.example.walletservice.dto.request.RegisterRequest;
import com.example.walletservice.dto.response.ApiResponse;
import com.example.walletservice.dto.response.AuthResponse;
import com.example.walletservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Login successful", response));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.registerAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Admin registration successful", response));
    }
}
