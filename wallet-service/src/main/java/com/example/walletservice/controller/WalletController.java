package com.example.walletservice.controller;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.request.WithdrawRequest;
import com.example.walletservice.dto.response.ApiResponse;
import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.dto.response.WalletOperationResponse;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse> deposit(@Valid @RequestBody DepositRequest request, @RequestHeader("X-User-Id") Long userId) {
        WalletOperationResponse response = walletService.deposit(userId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Deposit successful", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse> withdraw(@Valid @RequestBody WithdrawRequest request, @RequestHeader("X-User-Id") Long userId) {
        WalletOperationResponse response = walletService.withdraw(userId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Withdrawal successful", response));
    }

    @GetMapping({"/transactions", "/history"})
    public ResponseEntity<ApiResponse> getTransactionHistory(@RequestHeader("X-User-Id") Long userId) {
        List<TransactionResponse> transactions = walletService.getTransactionHistory(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ApiResponse(true, "Transaction history retrieved successfully", transactions));
    }
}
