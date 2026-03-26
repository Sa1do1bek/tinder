package com.example.tinder.dto.subsciption;

import com.example.tinder.enums.SubscriptionType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDto {
    private Long id;
    private String code;
    private SubscriptionType type;
    private int months;
    private BigDecimal price;
    private BigDecimal monthly_price;

    private Double discountPercent;
    private BigDecimal oldPrice;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionPlanCreateDto {
        private String code;
        private SubscriptionType type;
        private int months;
        private BigDecimal price;
        private Long subscriptionParentId;
    }
}
