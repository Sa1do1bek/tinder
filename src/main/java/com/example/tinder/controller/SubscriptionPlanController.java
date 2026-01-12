package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import com.example.tinder.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/subscription-plan")
public class SubscriptionPlanController {
    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/getAll")
    public ApiResponse<?> getAll() {
        return ApiResponse.success(subscriptionPlanService.getAll());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<?> create(@RequestBody SubscriptionPlanDto.SubscriptionPlanCreateDto subscriptionPlanCreateDto) {
        return ApiResponse.success(subscriptionPlanService.create(subscriptionPlanCreateDto));
    }


    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<?> delete(@RequestParam("subscriptionPlanId") Long subscriptionPlanId) {
        return ApiResponse.success(subscriptionPlanService.delete(subscriptionPlanId));
    }
}
