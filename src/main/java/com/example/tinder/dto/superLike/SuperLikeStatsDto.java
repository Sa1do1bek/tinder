package com.example.tinder.dto.superLike;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuperLikeStatsDto {
    private Integer totalSuperLikesAvailable;
    private Integer totalSuperLikesReceived;
    private Integer totalSuperLikesSent;
    private BigDecimal totalEarningsUZS;
    private BigDecimal currentBalanceUZS;
    private Long unpaidRewardsCount;
}