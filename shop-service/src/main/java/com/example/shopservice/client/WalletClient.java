package com.example.shopservice.client;

import com.example.shopservice.dto.request.WithdrawRequest;
import com.example.shopservice.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

// The name must EXACTLY match the name wallet-service registered with in Eureka!
@FeignClient(name = "wallet-service")
public interface WalletClient {

    // This must exactly match the endpoint URL in AuthController/WalletController!
    @PostMapping("/api/wallet/withdraw")
    ApiResponse withdraw(@RequestHeader("Authorization") String token, 
                         @RequestBody WithdrawRequest request);
}
