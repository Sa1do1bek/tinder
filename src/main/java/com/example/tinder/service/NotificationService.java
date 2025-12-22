package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import org.springframework.stereotype.Component;

@Component
public interface NotificationService {
    ApiResponse<?> sendNotification(NotificationDto notificationDto);
}
