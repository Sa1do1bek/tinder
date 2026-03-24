package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserSubscriptionService {
    ApiResponse<?> create(User user, Long subscriptionPlanId);

    void checkSubscriptions();

}
