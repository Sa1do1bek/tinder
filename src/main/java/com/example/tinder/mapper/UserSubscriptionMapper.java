package com.example.tinder.mapper;


import com.example.tinder.dto.subsciption.UserSubscriptionDto;
import com.example.tinder.entity.SubscriptionPlan;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserSubscription;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserSubscriptionMapper {

    public UserSubscriptionDto toDto(UserSubscription entity) {
        if (entity == null) return null;

        UserSubscriptionDto dto = new UserSubscriptionDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setSubscriptionPlanId(entity.getSubscriptionPlan().getId());
        dto.setStartedAt(entity.getCreatedAt());
        dto.setEndAt(entity.getEndAt());
        return dto;
    }

    public UserSubscription fromCreateDto(UserSubscriptionDto.UserSubscriptionCreateDto dto,
                                          User user,
                                          SubscriptionPlan subscriptionPlan) {
        if (dto == null) return null;

        UserSubscription entity = new UserSubscription();
        entity.setUser(user);
        entity.setSubscriptionPlan(subscriptionPlan);
        entity.setSubscriptionType(dto.getSubscriptionType());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setEndAt(dto.getEndAt());
        entity.setVisibility(true);
        return entity;
    }



}
