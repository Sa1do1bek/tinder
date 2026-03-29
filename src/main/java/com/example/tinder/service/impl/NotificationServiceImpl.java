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

        log.info("sendBatchNotification boshlandi: {} ta user", users.size());

        if (users == null || users.isEmpty()) {
            log.warn("User list bo'sh");
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }

        // 1. DB ga yozamiz — faqat bir marta
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
        log.info("DB ga {} ta notification yozildi", notifications.size());

        // 2. User ID lar
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();

        // 3. FCM tokenlarni olamiz
        List<FCM> fcmList = fcmRepository.findAllByUserIds(userIds);
        log.info("FCM topildi: {} ta", fcmList.size());

        List<String> tokens = fcmList.stream()
                .map(FCM::getFcmToken)
                .filter(token -> token != null && !token.isBlank())
                .distinct() // bir xil tokenlar bo'lmasin
                .toList();

        log.info("Valid token: {} ta", tokens.size());

        if (tokens.isEmpty()) {
            log.warn("Valid FCM token yo'q — notification DB ga yozildi, push yuborilmadi");
            return ApiResponse.success(SuccessType.NOTIFICATIONS_SAVED_NO_TOKENS);
        }

        // 4. 500 tadan batch qilib yuboramiz
        int successTotal = 0;
        int failureTotal = 0;

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
                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                successTotal += response.getSuccessCount();
                failureTotal += response.getFailureCount();

                log.info("Batch [{}-{}]: success={}, failure={}",
                        i, Math.min(i + 500, tokens.size()),
                        response.getSuccessCount(),
                        response.getFailureCount());

                // Expired tokenlarni o'chiramiz
                List<SendResponse> responses = response.getResponses();
                for (int j = 0; j < responses.size(); j++) {
                    SendResponse sendResponse = responses.get(j);
                    if (!sendResponse.isSuccessful()) {
                        String errorCode = sendResponse.getException().getMessagingErrorCode() != null
                                ? sendResponse.getException().getMessagingErrorCode().name()
                                : "UNKNOWN";
                        log.warn("Token {} xato: {}", batch.get(j), errorCode);

                        // Expired yoki invalid token bo'lsa — o'chiramiz
                        if (errorCode.equals("UNREGISTERED") || errorCode.equals("INVALID_ARGUMENT")) {
                            int finalJ = j;
                            fcmRepository.findByUserIdAndVisibilityTrue(
                                    fcmList.stream()
                                            .filter(f -> batch.get(finalJ).equals(f.getFcmToken()))
                                            .map(f -> f.getUser().getId())
                                            .findFirst()
                                            .orElse(-1L)
                            ).ifPresent(fcm -> {
                                fcm.setVisibility(false);
                                fcmRepository.save(fcm);
                                log.info("Expired token o'chirildi: {}", fcm.getId());
                            });
                        }
                    }
                }

            } catch (FirebaseMessagingException e) {
                log.error("Firebase batch xatolik: {}", e.getMessage(), e);
            }
        }

        log.info("Jami: success={}, failure={}", successTotal, failureTotal);
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
