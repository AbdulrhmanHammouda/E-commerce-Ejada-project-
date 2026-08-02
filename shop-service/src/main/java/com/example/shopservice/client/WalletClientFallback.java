package com.example.shopservice.client;

import com.example.shopservice.dto.request.WithdrawRequest;
import com.example.shopservice.exception.InsufficientFundsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WalletClientFallback implements WalletClient {

    @Override
    public ResponseEntity<Map<String, Object>> withdrawFunds(WithdrawRequest request, Long userId) {
        // This is executed when the Circuit Breaker trips (e.g. Wallet Service is down)
        throw new InsufficientFundsException("Wallet service is currently unavailable. Checkout failed. Please try again later.");
    }
}
