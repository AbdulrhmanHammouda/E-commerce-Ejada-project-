package com.example.shopservice.client;

import com.example.shopservice.exception.OutOfStockException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public ResponseEntity<Void> initializeStock(Long productId, Map<String, Integer> request) {
        throw new RuntimeException("Inventory service is currently unavailable.");
    }

    @Override
    public ResponseEntity<Void> deductStock(Long productId, Map<String, Integer> request) {
        throw new OutOfStockException("Inventory service is unavailable. Cannot process checkout.");
    }

    @Override
    public ResponseEntity<Void> restoreStock(Long productId, Map<String, Integer> request) {
        // Log fallback action, we might want to put this in a DLQ or retry later
        System.err.println("CIRCUIT BREAKER: Failed to restore stock for product " + productId + " due to Inventory Service unavailability.");
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<Void> addStock(Long productId, Map<String, Integer> request) {
        throw new RuntimeException("Inventory service is currently unavailable.");
    }

    @Override
    public Map<Long, Integer> getBulkStock(List<Long> productIds) {
        // Return an empty map or throw exception
        throw new RuntimeException("Inventory service is currently unavailable.");
    }
}
