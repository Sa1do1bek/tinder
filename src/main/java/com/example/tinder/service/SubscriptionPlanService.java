package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import org.springframework.stereotype.Service;

@Service
public interface SubscriptionPlanService {

    ApiResponse<?> create(SubscriptionPlanDto.SubscriptionPlanCreateDto subscriptionPlanCreateDto);

    ApiResponse<?> getAll();

    ApiResponse<?> delete(Long id);
}
