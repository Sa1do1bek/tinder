package com.example.tinder.dto.superLike;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWalletDto {
    private Long id;
    private Long userId;
    private String username;
    private Double balanceUZS;
    private Integer superLikeCount;
    private Integer totalSuperLikesEarned;
    private Double totalEarnedUZS;
}