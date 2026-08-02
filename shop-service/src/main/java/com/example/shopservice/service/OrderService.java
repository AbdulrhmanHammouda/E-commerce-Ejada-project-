package com.example.shopservice.service;

import com.example.shopservice.client.WalletClient;
import com.example.shopservice.client.InventoryClient;
import com.example.shopservice.dto.request.WithdrawRequest;
import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Order;
import com.example.shopservice.entity.OrderItem;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.OrderRepository;
import com.example.shopservice.exception.InsufficientFundsException;
import com.example.shopservice.exception.OutOfStockException;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final WalletClient walletClient;
    private final InventoryClient inventoryClient;

    public OrderService(CartItemRepository cartItemRepository, OrderRepository orderRepository, WalletClient walletClient, InventoryClient inventoryClient) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.walletClient = walletClient;
        this.inventoryClient = inventoryClient;
    }

    @CircuitBreaker(name = "checkoutService", fallbackMethod = "checkoutFallback")
    @Transactional
    public Order checkout(Long userId) {
        // 1. Get the user's cart
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total cost
        BigDecimal totalCost = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Deduct Inventory (Saga Step 1)
        List<CartItem> deductedItems = new ArrayList<>();
        try {
            for (CartItem cartItem : cartItems) {
                inventoryClient.deductStock(cartItem.getProduct().getId(), Map.of("quantity", cartItem.getQuantity()));
                deductedItems.add(cartItem);
            }
        } catch (FeignException e) {
            // If inventory fails (e.g. out of stock), rollback the ones we already deducted
            for (CartItem itemToRestore : deductedItems) {
                try {
                    inventoryClient.restoreStock(itemToRestore.getProduct().getId(), Map.of("quantity", itemToRestore.getQuantity()));
                } catch (Exception ex) {
                    System.err.println("CRITICAL: Failed to rollback inventory for product " + itemToRestore.getProduct().getId());
                }
            }
            throw new OutOfStockException("Checkout failed! Not enough stock for some items in your cart.");
        }

        // 3. Create the order in the database (Local Transaction)
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalCost);
        order.setStatus("PAID");

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            order.addOrderItem(orderItem);
        }

        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        // 4. Talk to the Wallet Service! (Saga Step 2)
        try {
            WithdrawRequest withdrawRequest = new WithdrawRequest(totalCost, "Checkout for " + cartItems.size() + " items");
            walletClient.withdrawFunds(withdrawRequest, userId);
        } catch (FeignException e) {
            // If Wallet fails, we MUST manually restore all inventory we deducted in Step 2!
            for (CartItem itemToRestore : cartItems) {
                try {
                    inventoryClient.restoreStock(itemToRestore.getProduct().getId(), Map.of("quantity", itemToRestore.getQuantity()));
                } catch (Exception ex) {
                    System.err.println("CRITICAL: Failed to rollback inventory for product " + itemToRestore.getProduct().getId() + " after wallet failure");
                }
            }
            // This exception rolls back the Local Transaction (Step 3)
            throw new InsufficientFundsException("Checkout failed! You don't have enough money in your wallet.");
        }

        return order;
    }

    public Order checkoutFallback(Long userId, Throwable t) {
        throw new RuntimeException("Checkout service is currently unavailable due to high load or downstream failures. Please try again later. Reason: " + t.getMessage());
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
