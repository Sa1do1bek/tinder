package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.NotificationPathType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface NotificationService {
    ApiResponse<?> saveOrUpdateStep(User user, String fcmToken);
    void saveNotifications(List<User> users, String title, String body, NotificationPathType type);// notification yuborilgani saqlab qo'yabmiz db ga
    ApiResponse<?> sendNotification(Long recipientId, String title, String body, NotificationPathType type);
    ApiResponse<?> sendBatchNotification(List<User> users, String title, String body,NotificationPathType type);// notification yuborishni birdan user lar ga 500tada bir urinishda yuborilyapti list qilib kim kirmagan bo'lsa 5 kun ichida

    ApiResponse<?> deleteToken(User user);
}
