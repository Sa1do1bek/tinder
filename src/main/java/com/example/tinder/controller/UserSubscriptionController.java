package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.UserSubscrationService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription")
public class UserSubscriptionController {
    private final UserSubscrationService userSubscrationService;


    @PostMapping("/create")
    public ApiResponse<?> create(@CurrentUser User user,
                                 @RequestParam("subscriptionPlanId") Long subscriptionPlanId) {
        return userSubscrationService.create(user, subscriptionPlanId);
    }






}
