package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import com.example.tinder.entity.SubscriptionPlan;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.SubscriptionPlanMapper;
import com.example.tinder.repository.SubscriptionPlanRepository;
import com.example.tinder.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public ApiResponse<?> create(SubscriptionPlanDto.SubscriptionPlanCreateDto subscriptionPlanCreateDto) {
        Optional<SubscriptionPlan> byCode = subscriptionPlanRepository.findByCode(subscriptionPlanCreateDto.getCode());
        if (byCode.isPresent()) {
            throw RestException.restThrow(ErrorType.SUBSCRIPTION_PLAN_ALREADY_EXISTS);
        }

        SubscriptionPlan subscriptionPlan = subscriptionPlanMapper.fromCreateDto(subscriptionPlanCreateDto);
        SubscriptionPlan save = subscriptionPlanRepository.save(subscriptionPlan);

        return ApiResponse.success(subscriptionPlanMapper.toDto(save), SuccessType.SUBSCRIPTION_PLAN_CREATED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> update(SubscriptionPlanDto.SubscriptionPlanUpdateDto subscriptionPlanUpdateDto) {
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(subscriptionPlanUpdateDto.getCode())
                .orElseThrow(() -> RestException.restThrow(ErrorType.SUBSCRIPTION_PLAN_NOT_FOUND));


        SubscriptionPlan subscriptionPlan = subscriptionPlanMapper.fromUpdateDto(subscriptionPlanUpdateDto, plan);
        SubscriptionPlan save = subscriptionPlanRepository.save(subscriptionPlan);

        return ApiResponse.success(subscriptionPlanMapper.toDto(save), SuccessType.SUBSCRIPTION_PLAN_UPDATE_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> getAll() {
        List<SubscriptionPlan> all = subscriptionPlanRepository.findAll();
        if (all.isEmpty()) {
            return ApiResponse.success(new ArrayList<>(), SuccessType.SUBSCRIPTION_PLAN_FETCHED_SUCCESSFULLY);
        }

        List<SubscriptionPlanDto> list = all.stream().map(subscriptionPlanMapper::toDto).toList();
        return ApiResponse.success(list, SuccessType.SUBSCRIPTION_PLAN_FETCHED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> delete(Long id) {
        Optional<SubscriptionPlan> byId = subscriptionPlanRepository.findById(id);
        if (byId.isEmpty()) throw RestException.restThrow(ErrorType.SUBSCRIPTION_PLAN_NOT_FOUND);
        subscriptionPlanRepository.delete(byId.get());
        return ApiResponse.success(SuccessType.SUBSCRIPTION_PLAN_DELETED_SUCCESSFULLY);
    }
}
