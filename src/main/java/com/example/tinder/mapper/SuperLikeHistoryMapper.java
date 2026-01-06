package com.example.tinder.mapper;

import com.example.tinder.dto.superLike.SuperLikeHistoryDto;
import com.example.tinder.entity.SuperLikeHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperLikeHistoryMapper {
    private final UserMapper userMapper;

    public SuperLikeHistoryDto toDto(SuperLikeHistory history) {
        if (history == null) return null;
        return SuperLikeHistoryDto.builder()
                .id(history.getId())
                .sender(userMapper.toBasicDto(history.getSender()))
                .receiver(userMapper.toBasicDto(history.getReceiver()))
                .rewardAmount(history.getRewardAmount())
                .isRewardPaid(history.getIsRewardPaid())
                .sentDate(history.getCreatedAt())
                .build();
    }

}
