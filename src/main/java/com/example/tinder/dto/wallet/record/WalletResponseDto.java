package com.example.tinder.dto.wallet.record;

import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponseDto(
        Long id,
        BigDecimal amount,
        BigDecimal balanceAfter,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt,
        PaymentMethod paymentMethod,
        String description,
        UserDetailsDto user) {}
