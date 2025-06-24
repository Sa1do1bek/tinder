package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import com.example.tinder.dto.NotificationMessage;
import com.example.tinder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Override
    public ApiResponse sendNotification(NotificationDto notificationDto) {
        simpMessagingTemplate.convertAndSend(
                "/topic/notification/" + notificationDto.getToUserId(),
                new NotificationMessage(notificationDto)
        );

        return
                ApiResponse.builder()
                        .message("Notification sent")
                        .status(HttpStatus.OK)
                        .build();
    }
}
