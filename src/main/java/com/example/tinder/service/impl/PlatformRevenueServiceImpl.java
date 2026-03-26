package com.example.tinder.service.impl;

import com.example.tinder.entity.PlatformRevenue;
import com.example.tinder.entity.SuperLikePackage;
import com.example.tinder.entity.SuperLikePurchase;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.RevenueType;
import com.example.tinder.repository.PlatformRevenueRepository;
import com.example.tinder.repository.WalletTransactionRepository;
import com.example.tinder.service.PlatformRevenueService;
import com.example.tinder.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRevenueServiceImpl implements PlatformRevenueService {
    private final PlatformRevenueRepository platformRevenueRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private static final BigDecimal REWARD_PER_SUPERLIKE = new BigDecimal("5000.0");
    private static final BigDecimal WITHDRAW_COMMISSION_RATE = new BigDecimal("5.0"); // 5% komissiya


    @Override
    public void recordPlatformRevenue(SuperLikePurchase purchase, SuperLikePackage pkg,Long userId) {
        BigDecimal totalAmount = purchase.getAmountPaid();
        BigDecimal totalRewards = REWARD_PER_SUPERLIKE.multiply(BigDecimal.valueOf(pkg.getQuantity()));
        BigDecimal platformAmount = totalAmount.subtract(totalRewards);

        WalletTransaction walletTransaction = walletTransactionRepository.findByUserId(userId).orElse(null);

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

        log.info("Platform INCOME recorded: total={}, platformAmount={}, potentialRewards={}",
                totalAmount, platformAmount, totalRewards);
    }

    @Override
    public void recordPlatformExpense(WalletTransaction transaction, BigDecimal withdrawAmount) {
        BigDecimal commissionAmount = withdrawAmount.multiply(WITHDRAW_COMMISSION_RATE).divide(new BigDecimal("100"));

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

        log.info("Platform EXPENSE recorded: withdrawAmount={}, commission={}, user={}",
                withdrawAmount, commissionAmount, transaction.getUser().getUsername());

    }
}
