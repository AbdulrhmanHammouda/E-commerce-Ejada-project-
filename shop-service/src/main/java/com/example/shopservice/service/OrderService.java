package com.example.shopservice.service;

import com.example.shopservice.client.WalletClient;
import com.example.shopservice.dto.request.WithdrawRequest;
import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Order;
import com.example.shopservice.entity.OrderItem;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.OrderRepository;
import com.example.shopservice.exception.InsufficientFundsException;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final WalletClient walletClient;

    public OrderService(CartItemRepository cartItemRepository, OrderRepository orderRepository, WalletClient walletClient) {
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.walletClient = walletClient;
    }

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

        // 2. Create the order in the database FIRST (so it rolls back if Wallet fails)
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

        // 3. Talk to the Wallet Service! (HTTP Call should be LAST)
        try {
            WithdrawRequest withdrawRequest = new WithdrawRequest(totalCost, "Checkout for " + cartItems.size() + " items");
            walletClient.withdrawFunds(withdrawRequest, userId);
        } catch (FeignException e) {
            // This exception will cause the @Transactional to rollback the DB save and delete!
            throw new InsufficientFundsException("Checkout failed! You don't have enough money in your wallet.");
        }

        return order;
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
