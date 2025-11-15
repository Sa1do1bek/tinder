package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import com.example.tinder.dto.NotificationMessage;
import com.example.tinder.entity.Device;
import com.example.tinder.entity.User;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.repository.DeviceRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("tinderNotificationService")
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final FirebaseNotificationService firebaseNotificationService;
    private final DeviceRepository deviceRepository;

    @Override
    public ApiResponse sendNotification(NotificationDto notificationDto) {
        //qabul qiluvchi userni devicelarini qidiramiz
        List<Device> allByUserId = deviceRepository.findAllByUserId(notificationDto.getToUserId());

        //devicelarga notification yuboramiz
        for (Device device : allByUserId) {
            firebaseNotificationService
                    .sendNotification(device.getFcmToken(), "Yangi xabar !", notificationDto.getMessage(), "", new HashMap<>());
        }

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
