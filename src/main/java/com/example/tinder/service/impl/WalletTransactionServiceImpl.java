package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.WalletTransactionDto;
import com.example.tinder.entity.SuperLikeHistory;
import com.example.tinder.entity.User;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.mapper.WalletTransactionMapper;
import com.example.tinder.repository.WalletTransactionRepository;
import com.example.tinder.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    public void createTransaction(User user, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, SuperLikeHistory history, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter != null ? balanceAfter : new BigDecimal("0.0"))
                .superLikeHistory(history)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        walletTransactionRepository.save(transaction);
    }

    @Override
    public ApiResponse<?> getMyTransactions(Long userId, Pageable pageable) {
        Page<WalletTransaction> transactions = walletTransactionRepository.findAllByUserId(userId, pageable);

        Page<WalletTransactionDto> dtos = transactions.map(walletTransactionMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, SuccessType.TRANSACTIONS_FETCHED_SUCCESSFULLY);
    }
}
