package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.FCM;
import com.example.tinder.entity.Notification;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.NotificationPathType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.FCMRepository;
import com.example.tinder.repository.NotificationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final FCMRepository fcmRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<?> saveOrUpdateStep(User user, String fcmToken) {
        FCM fcm = fcmRepository.findByUserIdAndVisibilityTrue(user.getId()).orElse(new FCM());
        fcm.setUser(user);
        fcm.setFcmToken(fcmToken);
        fcmRepository.save(fcm);

        return ApiResponse.success(SuccessType.FCM_TOKEN_SAVED_SUCCESSFULLY);
    }

    @Override
    @Async("NotificationTaskExecutor")
    public ApiResponse<?> sendNotification(Long recipientId, String title, String body, NotificationPathType type) {

        User recipient = userRepository.findById(recipientId).orElse(null);
        if (recipient == null) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);

        Notification notification = Notification.builder()
                .user(recipient)
                .title(title)
                .body(body)
                .pathType(type)
                .read(false)
                .build();
        notificationRepository.save(notification);

        FCM fcm = fcmRepository.findByUserIdAndVisibilityTrue(recipientId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.FCM_TOKEN_NOT_FOUND));

        String token = fcm.getFcmToken();

        if (token == null || token.isBlank()) {
            throw RestException.restThrow(ErrorType.INVALID_FCM_TOKEN);
        }

        Message message = Message.builder()
                .setToken(token)
                .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("Firebase xabari yuborildi: {}", recipientId);
        } catch (FirebaseMessagingException e) {
            log.error("Firebase xatolik: {}", e.getMessage());
        }

        return ApiResponse.success(SuccessType.NOTIFICATION_SENT_SUCCESSFULLY);
    }
}
