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

    public SubscriptionPlan fromUpdateDto(SubscriptionPlanDto.SubscriptionPlanUpdateDto dto, SubscriptionPlan entity) {
        if (dto == null || entity == null) return entity;

        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }

        if (dto.getMonths() > 0) {
            entity.setMonths(dto.getMonths());
        }

        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }


}
