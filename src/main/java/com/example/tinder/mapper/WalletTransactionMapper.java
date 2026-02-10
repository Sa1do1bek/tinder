package com.example.tinder.mapper;

import com.example.tinder.dto.superLike.WalletTransactionDto;
import com.example.tinder.entity.WalletTransaction;
import org.springframework.stereotype.Component;

@Component
public class WalletTransactionMapper {

    public WalletTransactionDto toDto(WalletTransaction transaction) {
        if (transaction == null) return null;
        return WalletTransactionDto.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .transactionDate(transaction.getCreatedAt())
                .paymentMethod(transaction.getPaymentMethod() == null ? "" : transaction.getPaymentMethod().name()
                ).build();
    }

}
