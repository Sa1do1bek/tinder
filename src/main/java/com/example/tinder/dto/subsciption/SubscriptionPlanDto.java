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
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionPlanUpdateDto {
        private String code;
        private SubscriptionType type;
        private int months;
        private BigDecimal price;
    }
}
