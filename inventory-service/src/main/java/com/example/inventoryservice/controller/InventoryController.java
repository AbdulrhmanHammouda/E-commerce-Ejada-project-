package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.StockRequest;
import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                inventory.getQuantity(),
                inventory.getUpdatedAt()
        );
    }

    @PostMapping("/initialize/{productId}")
    public ResponseEntity<InventoryResponse> initializeStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        Inventory inventory = inventoryService.initializeStock(productId, request.getQuantity());
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable Long productId) {
        int quantity = inventoryService.getStock(productId);
        return ResponseEntity.ok(Map.of("productId", productId, "quantity", quantity));
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<InventoryResponse> addStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        Inventory inventory = inventoryService.addStock(productId, request.getQuantity());
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    @PostMapping("/deduct/{productId}")
    public ResponseEntity<InventoryResponse> deductStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        Inventory inventory = inventoryService.deductStock(productId, request.getQuantity());
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    @PostMapping("/restore/{productId}")
    public ResponseEntity<InventoryResponse> restoreStock(@PathVariable Long productId, @RequestBody StockRequest request) {
        Inventory inventory = inventoryService.restoreStock(productId, request.getQuantity());
        return ResponseEntity.ok(mapToResponse(inventory));
    }

    @PostMapping("/stock-bulk")
    public ResponseEntity<Map<Long, Integer>> getBulkStock(@RequestBody List<Long> productIds) {
        Map<Long, Integer> stockMap = inventoryService.getBulkStock(productIds);
        return ResponseEntity.ok(stockMap);
    }
}
