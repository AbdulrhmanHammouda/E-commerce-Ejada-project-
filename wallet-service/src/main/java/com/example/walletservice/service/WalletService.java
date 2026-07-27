package com.example.walletservice.service;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.request.WithdrawRequest;
import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.dto.response.WalletOperationResponse;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.TransactionType;
import com.example.walletservice.entity.User;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.InsufficientBalanceException;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.repository.WalletRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public WalletOperationResponse deposit(String userEmail, DepositRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userEmail));

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new WalletOperationResponse(request.getAmount(), newBalance, savedTransaction.getId());
    }

    @Transactional
    public WalletOperationResponse withdraw(String userEmail, WithdrawRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserIdWithLock(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userEmail));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient funds. Current balance: " + wallet.getBalance());
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new WalletOperationResponse(request.getAmount(), newBalance, savedTransaction.getId());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userEmail));

        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());

        return transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getType(),
                        t.getAmount(),
                        t.getDescription(),
                        t.getCreatedAt()
                ))
                .toList();
    }
}
