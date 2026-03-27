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
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public void saveNotifications(List<User> users, String title, String body, NotificationPathType type) {

        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .pathType(type)
                        .read(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
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

    @Override
    public ApiResponse<?> sendBatchNotification(List<User> users, String title, String body, NotificationPathType type) {
        if (users == null || users.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }

        // 1. DB ga notification yozamiz
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .title(title)
                        .body(body)
                        .pathType(type)
                        .read(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);

        // 2. userId larni olamiz
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        // 3. bitta query bilan barcha FCM larni olamiz
        List<FCM> fcmList = fcmRepository.findAllByUserIds(userIds);

        List<String> tokens = fcmList.stream()
                .map(FCM::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .toList();

        if (tokens.isEmpty()) {
            log.warn("No valid FCM tokens");
            return ApiResponse.success(SuccessType.NOTIFICATIONS_SAVED_NO_TOKENS);
        }

        //4. 500 tadan batch
        for (int i = 0; i < tokens.size(); i += 500) {

            List<String> batch = tokens.subList(i, Math.min(i + 500, tokens.size()));

            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(
                            com.google.firebase.messaging.Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);

                log.info("Batch sent: success={}, failure={}",
                        response.getSuccessCount(),
                        response.getFailureCount());

            } catch (FirebaseMessagingException e) {
                log.error("Batch send error: {}", e.getMessage());
            }
        }

        return ApiResponse.success(SuccessType.BATCH_NOTIFICATION_SENT_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> deleteToken(User user) {
        Optional<User> currentUser = userRepository.findById(user.getId());
        if (currentUser.isEmpty()) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        User user1 = currentUser.get();
        user1.setFirebaseUid(null);
        userRepository.save(user1);

        Optional<FCM> byUserIdAndVisibilityTrue = fcmRepository.findByUserIdAndVisibilityTrue(user1.getId());
        byUserIdAndVisibilityTrue.ifPresent(fcm -> fcm.setVisibility(false));

        return ApiResponse.success(SuccessType.FCM_TOKEN_DELETED_SUCCESSFULLY);

    }
}
