package com.example.tinder.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum SubscriptionPlan {
    GOLD_1_MONTH(SubscriptionType.GOLD, 1, new BigDecimal(99000)),
    GOLD_6_MONTH(SubscriptionType.GOLD, 6, new BigDecimal(499000)),
    GOLD_12_MONTH(SubscriptionType.GOLD, 12, new BigDecimal(899000));


    private final SubscriptionType type;
    private final int months;
    private final BigDecimal price;
}