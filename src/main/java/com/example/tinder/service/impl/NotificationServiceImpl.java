package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import com.example.tinder.dto.message.NotificationMessage;
import com.example.tinder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("tinderNotificationService")
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Override
    public ApiResponse<?> sendNotification(NotificationDto notificationDto) {
        simpMessagingTemplate.convertAndSend(
                "/topic/notification/" + notificationDto.getToUserId(),
                new NotificationMessage(notificationDto)
        );

        return ApiResponse.success("Notification sent");
    }
}
