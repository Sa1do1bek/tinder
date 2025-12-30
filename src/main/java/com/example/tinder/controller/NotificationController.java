package com.example.tinder.controller;

import com.example.tinder.dto.FCMDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.NotificationPathType;
import com.example.tinder.service.NotificationService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;


    /**
     * Mobil ilova har gal login bo'lganda yoki token yangilanganda
     * shu endpointga request yuboradi.
     */
    @PostMapping("/token")
    public ApiResponse<?> saveToken(@RequestBody FCMDto.CreateFCM request,
                                    @CurrentUser User currentUser) {

        return notificationService.saveOrUpdateStep(currentUser, request.getFcmToken());
    }


    /**
     * Test qilish uchun endpoint.
     * Istalgan userga notification yuborib ko'rish mumkin.
     */
    @PostMapping("/send")
    public ApiResponse<?> sendTestNotification(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String body) {

        return notificationService.sendNotification(userId, title, body, NotificationPathType.NEWS_MESSAGE);
    }

}
