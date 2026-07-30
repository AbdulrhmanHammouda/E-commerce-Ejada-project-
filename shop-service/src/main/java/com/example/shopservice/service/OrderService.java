package com.example.shopservice.service;

import com.example.shopservice.client.WalletClient;
import com.example.shopservice.dto.WithdrawRequestDto;
import com.example.shopservice.entity.CartItem;
import com.example.shopservice.entity.Order;
import com.example.shopservice.entity.OrderItem;
import com.example.shopservice.repository.CartItemRepository;
import com.example.shopservice.repository.OrderRepository;
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
        // 1. Get the cart
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cannot checkout. Your cart is empty!");
        }

        // 2. Calculate the total cost
        BigDecimal totalCost = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity()));
            totalCost = totalCost.add(itemTotal);
        }

        // 3. Talk to the Wallet Service! Attempt to withdraw the funds.
        try {
            WithdrawRequestDto withdrawRequest = new WithdrawRequestDto(totalCost, "Checkout for " + cartItems.size() + " items");
            walletClient.withdrawFunds(withdrawRequest, userId);
        } catch (FeignException e) {
            // If WalletService returns 400 (Insufficient Funds), OpenFeign throws an exception
            throw new RuntimeException("Checkout failed! You don't have enough money in your wallet.");
        }

        // 4. Create the Order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalCost);
        order.setStatus("PAID");

        // 5. Convert CartItems to OrderItems
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
            order.addOrderItem(orderItem); // Handles bi-directional relationship
        }

        Order savedOrder = orderRepository.save(order);

        // 6. Empty the cart!
        cartItemRepository.deleteAll(cartItems);

        return savedOrder;
    }

    public List<Order> getOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
