package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.NotificationDto;
import com.example.tinder.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    @Qualifier("tinderNotificationService")
    private NotificationService notificationService;


    public NotificationController( NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostMapping("/send")
    public ApiResponse sendNotification(@RequestBody NotificationDto notificationDto) {
      return notificationService.sendNotification(notificationDto);
    }
}
