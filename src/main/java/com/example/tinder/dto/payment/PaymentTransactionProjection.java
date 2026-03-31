package com.example.tinder.dto.payment;

import com.example.tinder.enums.payment.PaymeTransactionState;
import com.example.tinder.enums.payment.PaymentMethod;
import com.example.tinder.enums.payment.PaymentStatusForClick;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentTransactionProjection {
        Long getId();
        BigDecimal getAmount();
        Long getUserId();
        PaymentMethod getPaymentMethod();

        // Payme
        Long getCreateTime();
        Long getPerformTime();
        Long getCancelTime();
        PaymeTransactionState getState();
        Integer getReason();

        // Click
        Long getClickPaydocId();
        LocalDateTime getPrepareTime();
        LocalDateTime getCompleteTime();
        LocalDateTime getClickCancelTime();
        PaymentStatusForClick getClickStatus();

        // Common
        Long getMerchantTransactionId();
        Long getWalletTransactionId();
}
