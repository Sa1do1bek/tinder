package com.example.tinder.dto.superLike;

import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionDto {
    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private TransactionStatus status;
    private LocalDateTime transactionDate;
}