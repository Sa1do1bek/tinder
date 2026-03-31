package com.example.tinder.dto.wallet;

import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface WalletTransactionProjection {
    Long getId();
    BigDecimal getAmount();
    BigDecimal getBalanceAfter();
    TransactionType getType();
    TransactionStatus getStatus();
    LocalDateTime getCreatedAt();

    PaymentMethod getPaymentMethod();
    String getDescription();

    // UserDetailsDto
    UserWalletProjection getUser();

}
