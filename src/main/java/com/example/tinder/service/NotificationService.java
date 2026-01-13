package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.NotificationPathType;
import org.springframework.stereotype.Component;

@Component
public interface NotificationService {
    ApiResponse<?> saveOrUpdateStep(User user, String fcmToken);
    ApiResponse<?> sendNotification(Long recipientId, String title, String body, NotificationPathType type);

    ApiResponse<?> deleteToken(User user);
}
