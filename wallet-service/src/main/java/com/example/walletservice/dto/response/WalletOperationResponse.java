package com.example.walletservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationResponse {
    private BigDecimal amount;
    private BigDecimal balance;
    private Long transactionId;
}
