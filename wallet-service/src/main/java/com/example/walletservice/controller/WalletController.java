package com.example.walletservice.controller;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.request.WithdrawRequest;
import com.example.walletservice.dto.response.ApiResponse;
import com.example.walletservice.dto.response.WalletOperationResponse;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse> deposit(@Valid @RequestBody DepositRequest request, Authentication authentication) {
        WalletOperationResponse response = walletService.deposit(authentication.getName(), request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Deposit successful", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@Valid @RequestBody WithdrawRequest request, Authentication authentication) {
        WalletOperationResponse response = walletService.withdraw(authentication.getName(), request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Withdrawal successful", response));
    }
}
