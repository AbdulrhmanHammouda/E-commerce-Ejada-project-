package com.example.shopservice.client;

import com.example.shopservice.dto.WithdrawRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

// We connect to the "wallet-service" registered in Eureka
@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/api/wallet/withdraw")
    ResponseEntity<Map<String, Object>> withdrawFunds(
            @RequestBody WithdrawRequestDto request,
            @RequestHeader("X-User-Id") Long userId
    );
}
