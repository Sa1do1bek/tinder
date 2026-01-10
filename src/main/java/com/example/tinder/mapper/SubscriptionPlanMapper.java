package com.example.tinder.mapper;

import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import com.example.tinder.entity.SubscriptionPlan;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanDto toDto(SubscriptionPlan dto) {
        if (dto == null) return null;

        SubscriptionPlanDto entity = new SubscriptionPlanDto();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setMonths(dto.getMonths());
        entity.setPrice(dto.getPrice());
        entity.setType(dto.getType());
        return entity;
    }

    public SubscriptionPlan fromCreateDto(SubscriptionPlanDto.SubscriptionPlanCreateDto dto) {
        if (dto == null) return null;

        SubscriptionPlan entity = new SubscriptionPlan();
        entity.setCode(dto.getCode());
        entity.setMonths(dto.getMonths());
        entity.setPrice(dto.getPrice());
        entity.setType(dto.getType());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVisibility(true);
        return entity;
    }

}
