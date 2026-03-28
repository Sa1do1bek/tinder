package com.example.tinder.service.impl;

import com.example.tinder.entity.PlatformRevenue;
import com.example.tinder.entity.SuperLikePackage;
import com.example.tinder.entity.SuperLikePurchase;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.RevenueType;
import com.example.tinder.repository.PlatformRevenueRepository;
import com.example.tinder.service.PlatformRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRevenueServiceImpl implements PlatformRevenueService {
    private final PlatformRevenueRepository platformRevenueRepository;
    private static final BigDecimal REWARD_PER_SUPERLIKE = new BigDecimal("5000.0");
    private static final BigDecimal WITHDRAW_COMMISSION_RATE = new BigDecimal("5.0"); // 5% komissiya


    @Override
    public void recordPlatformRevenue(SuperLikePurchase purchase, SuperLikePackage pkg, WalletTransaction walletTransaction) {

        if (walletTransaction == null) {
            log.warn("WalletTransaction is null for purchase ID: {}", purchase.getId());
        }

        BigDecimal totalAmount = purchase.getAmountPaid();
        BigDecimal totalRewards = REWARD_PER_SUPERLIKE.multiply(BigDecimal.valueOf(pkg.getQuantity()));
        BigDecimal platformAmount = totalAmount.subtract(totalRewards);

        PlatformRevenue revenue = PlatformRevenue.builder()
                .type(RevenueType.INCOME) // KIRIM
                .purchase(purchase)
                .walletTransaction(walletTransaction)
                .totalAmount(totalAmount)
                .userRewardAmount(totalRewards) // Potensial mukofot
                .platformAmount(platformAmount) // Platform ulushi (musbat)
                .commissionRate(pkg.getPlatformCommission())
                .build();

        platformRevenueRepository.save(revenue);

        log.info("✅ Platform INCOME saved | PurchaseId: {} | WalletTxId: {} | PlatformAmount: {}",
                purchase.getId(),
                walletTransaction != null ? walletTransaction.getId() : "NULL",
                platformAmount);
    }

    @Override
    public void recordPlatformExpense(WalletTransaction transaction, BigDecimal withdrawAmount) {
        BigDecimal commissionAmount = withdrawAmount
                .multiply(WITHDRAW_COMMISSION_RATE)
                .divide(new BigDecimal("100"), RoundingMode.HALF_UP);

        PlatformRevenue expense = PlatformRevenue.builder()
                .type(RevenueType.EXPENSE) // CHIQIM
                .purchase(null)
                .walletTransaction(transaction)
                .totalAmount(withdrawAmount) // Yechib olingan summa
                .userRewardAmount(withdrawAmount) // User olgan summa
                .platformAmount(commissionAmount.negate()) // Minus (chiqim)
                .commissionRate(WITHDRAW_COMMISSION_RATE)
                .build();

        platformRevenueRepository.save(expense);

        log.info("✅ Platform EXPENSE saved | WalletTxId: {} | Commission: {}",
                transaction.getId(), commissionAmount);

    }
}
