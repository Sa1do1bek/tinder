package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.WithdrawRequest;
import com.example.tinder.entity.Card;
import com.example.tinder.entity.PlatformRevenue;
import com.example.tinder.entity.User;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.*;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.CardRepository;
import com.example.tinder.repository.PlatformRevenueRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.repository.WalletTransactionRepository;
import com.example.tinder.service.PlatformRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {
    
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository transactionRepository;
    private final PlatformRevenueService platformRevenueService;

    private static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("50000"); // 50,000 som

    @Transactional
    public ApiResponse<?> withdrawBalance(Long userId, WithdrawRequest request) {
        log.info("User {} attempting to withdraw {} UZS", userId, request.getAmount());


        if (request.getAmount().compareTo(MIN_WITHDRAW_AMOUNT) < 0) {
            throw RestException.restThrow(ErrorType.MINIMUM_WITHDRAW_NOT_MET);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        
        Card card = cardRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        BigDecimal currentBalance = card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO;
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw RestException.restThrow(ErrorType.INSUFFICIENT_BALANCE);
        }

        
        // Card'dan pul ayirish
        BigDecimal newBalance = currentBalance.subtract(request.getAmount());
        card.setBalance(newBalance);
        cardRepository.save(card);
        
        // Wallet transaction yaratish
        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAW)
                .amount(request.getAmount().negate()) // Minus
                .balanceAfter(newBalance)
                .superLikeHistory(null)
                .description("Pul yechib olindi: " + request.getBankAccount() + " (" + request.getBankName() + ")")
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);

        platformRevenueService.recordPlatformExpense(transaction, request.getAmount());

        log.info("User {} withdrew {} UZS successfully. New balance: {}",
                userId, request.getAmount(), newBalance);
        
        return ApiResponse.success(null, SuccessType.WITHDRAWAL_SUCCESSFUL);
    }

}