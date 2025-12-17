package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.enums.SwipeStatus;
import org.springframework.stereotype.Component;

@Component
public interface SwipeService {
    ApiResponse<?> swipe(Long swiperId, Long swipedId, SwipeStatus status);
    ApiResponse<?> getSwipesByUser(Long userId);
}
