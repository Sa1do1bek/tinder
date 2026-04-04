package com.example.tinder.dto.subsciption;

import com.example.tinder.enums.SubscriptionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionDto {
    private Long id;
    private Long userId;
    private SubscriptionType SubscriptionType;
    private Long SubscriptionPlanId;
    private LocalDateTime startedAt;
    private LocalDateTime endAt;




    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSubscriptionCreateDto {
        private Long userId;
        private SubscriptionType SubscriptionType;
        private Long SubscriptionPlanId;
        private LocalDateTime startedAt;
        private LocalDateTime endAt;
    }
}
