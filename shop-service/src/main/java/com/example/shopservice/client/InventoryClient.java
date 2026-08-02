package com.example.shopservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.List;

@FeignClient(name = "inventory-service", fallback = InventoryClientFallback.class)
public interface InventoryClient {

    @PostMapping("/api/inventory/initialize/{productId}")
    ResponseEntity<Void> initializeStock(@PathVariable("productId") Long productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/api/inventory/deduct/{productId}")
    ResponseEntity<Void> deductStock(@PathVariable("productId") Long productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/api/inventory/restore/{productId}")
    ResponseEntity<Void> restoreStock(@PathVariable("productId") Long productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/api/inventory/add/{productId}")
    ResponseEntity<Void> addStock(@PathVariable("productId") Long productId, @RequestBody Map<String, Integer> request);

    @PostMapping("/api/inventory/stock-bulk")
    Map<Long, Integer> getBulkStock(@RequestBody List<Long> productIds);
}
