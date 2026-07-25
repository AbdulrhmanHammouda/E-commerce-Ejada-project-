package com.example.walletservice.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.walletservice.entity.Wallet;

public interface WalletRepository
        extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);
}
