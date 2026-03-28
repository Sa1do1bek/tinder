package com.example.tinder.service;

import com.example.tinder.entity.SuperLikePackage;
import com.example.tinder.entity.SuperLikePurchase;
import com.example.tinder.entity.WalletTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface PlatformRevenueService {

    void recordPlatformRevenue(SuperLikePurchase purchase, SuperLikePackage pkg, WalletTransaction walletTransaction);

    void recordPlatformExpense(WalletTransaction transaction, BigDecimal withdrawAmount);
}
