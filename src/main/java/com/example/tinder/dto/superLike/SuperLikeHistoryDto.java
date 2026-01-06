package com.example.tinder.dto.superLike;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuperLikeHistoryDto {
    private Long id;
    private UserBasicDto sender;
    private UserBasicDto receiver;
    private BigDecimal rewardAmount;
    private Boolean isRewardPaid;
    private LocalDateTime sentDate;
}