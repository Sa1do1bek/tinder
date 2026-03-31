package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.SuperLikeHistory;
import com.example.tinder.entity.User;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public interface WalletTransactionService {

    void createTransaction(User user, TransactionType type, BigDecimal amount,
                           BigDecimal balanceAfter, SuperLikeHistory history, String description);

    ApiResponse<?> getMyTransactions(Long userId, Pageable pageable);

    ApiResponse<Page<?>> getAllWallet(LocalDate start, LocalDate end, PaymentMethod paymentMethod, int page, int size);
}
