package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import com.example.tinder.entity.SubscriptionPlan;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SubscriptionType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.SubscriptionPlanMapper;
import com.example.tinder.repository.SubscriptionPlanRepository;
import com.example.tinder.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    @Transactional
    public ApiResponse<?> create(SubscriptionPlanDto.SubscriptionPlanCreateDto subscriptionPlanCreateDto) {

        childValidation(subscriptionPlanCreateDto);

        /** SubscriptionPlan "CREATE" bo'lmoqda*/
        SubscriptionPlan subscriptionPlan = subscriptionPlanMapper.fromCreateDto(subscriptionPlanCreateDto);

        if (subscriptionPlanCreateDto.getSubscriptionParentId() != null) {

            SubscriptionPlan parent = getParent(subscriptionPlanCreateDto.getSubscriptionParentId());

            /**Parent disable*/
            parent.setVisibility(false);

            /**Parent ni child ga bog'lash*/
            subscriptionPlan.setParent(parent);
            subscriptionPlanRepository.save(parent);
        }

        SubscriptionPlan save = subscriptionPlanRepository.save(subscriptionPlan);

        return ApiResponse.success(subscriptionPlanMapper.toDto(save),
                SuccessType.SUBSCRIPTION_PLAN_CREATED_SUCCESSFULLY);
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

    private void childValidation(SubscriptionPlanDto.SubscriptionPlanCreateDto dto) {

        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw RestException.restThrow(ErrorType.INVALID_SUBSCRIPTION_PRICE);
        }

        if (dto.getMonths() <= 0) {
            throw RestException.restThrow(ErrorType.INVALID_SUBSCRIPTION_MONTH);
        }

        if (dto.getCode().isBlank()) {
            throw RestException.restThrow(ErrorType.INVALID_SUBSCRIPTION_CODE);
        }

        if (!dto.getType().equals(SubscriptionType.GOLD)) {
            throw RestException.restThrow((ErrorType.INVALID_SUBSCRIPTION_TYPE));
        }
    }

    private SubscriptionPlan getParent(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.SUBSCRIPTION_PLAN_NOT_FOUND));
    }

    private void parentValidation(SubscriptionPlan parent) {

    }


}
