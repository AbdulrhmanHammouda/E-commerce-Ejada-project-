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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.walletservice.dto.response.PaginatedResponse;

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
    public WalletOperationResponse deposit(Long userId, DepositRequest request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

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
    public WalletOperationResponse withdraw(Long userId, WithdrawRequest request) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

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
    public PaginatedResponse<TransactionResponse> getTransactionHistory(Long userId, Pageable pageable) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        Page<Transaction> transactionPage = transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);

        List<TransactionResponse> responseList = transactionPage.getContent().stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getType(),
                        t.getAmount(),
                        t.getDescription(),
                        t.getCreatedAt()
                ))
                .toList();

        return new PaginatedResponse<>(
                responseList,
                transactionPage.getNumber(),
                transactionPage.getTotalPages(),
                transactionPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        return wallet.getBalance();
    }
}
