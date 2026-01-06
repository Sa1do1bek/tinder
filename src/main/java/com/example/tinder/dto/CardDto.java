package com.example.tinder.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Long id;
    private Long userId;
    private String number;
    private BigDecimal balance;
    private Integer superLikeCount = 0;
    private Integer totalSuperLikesEarned = 0;
    private BigDecimal totalEarnedUZS;
    
}