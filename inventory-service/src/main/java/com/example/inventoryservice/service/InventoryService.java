package com.example.inventoryservice.service;

import com.example.inventoryservice.entity.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public Inventory initializeStock(Long productId, int quantity) {
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new RuntimeException("Inventory already initialized for product: " + productId);
        }
        return inventoryRepository.save(new Inventory(productId, quantity));
    }

    public int getStock(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0); // If no inventory found, return 0 instead of throwing an exception (safer for bulk view)
    }

    public Map<Long, Integer> getBulkStock(List<Long> productIds) {
        List<Inventory> inventories = inventoryRepository.findByProductIdIn(productIds);
        Map<Long, Integer> stockMap = new HashMap<>();
        
        // Initialize all requested IDs with 0 default
        for (Long id : productIds) {
            stockMap.put(id, 0);
        }
        
        // Override with actual quantities found in DB
        for (Inventory inv : inventories) {
            stockMap.put(inv.getProductId(), inv.getQuantity());
        }
        
        return stockMap;
    }

    @Transactional
    public Inventory addStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        inventory.setQuantity(inventory.getQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory deductStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

        if (inventory.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId + ". Available: " + inventory.getQuantity());
        }

        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventoryRepository.save(inventory);
    }
    
    @Transactional
    public Inventory restoreStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        inventory.setQuantity(inventory.getQuantity() + quantity);
        return inventoryRepository.save(inventory);
    }
}
