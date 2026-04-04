package com.example.tinder.scheduler;

import com.example.tinder.entity.User;
import com.example.tinder.enums.NotificationMessages;
import com.example.tinder.enums.NotificationPathType;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class InactiveUserScheduler {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private NotificationMessages getRandomMessage() {
        NotificationMessages[] values = NotificationMessages.values();
        return values[new Random().nextInt(values.length)];
    }

    @Scheduled(cron = "0 0 22 * * ?", zone = "Asia/Tashkent")
    public void sendInactiveUserNotifications() {
        log.info("=== Inactive scheduler boshlandi ===");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveDaysAgo = now.minusDays(5);
        LocalDateTime twoDaysAgo = now.minusDays(2);

        List<User> users = userRepository.findUsersForInactiveNotification(
                fiveDaysAgo,
                twoDaysAgo
        );

        log.info("Inactive users topildi: {}", users.size());

        if (users.isEmpty()) {
            log.info("User yo'q, scheduler to'xtadi");
            return;
        }

        NotificationMessages message = getRandomMessage();
        log.info("Xabar tanlandi: {}", message.getTitle());

        try {

            // 1. DB ga yozamiz
            notificationService.saveNotifications(
                    users,
                    message.getTitle(),
                    message.getBody(),
                    NotificationPathType.NEWS_MESSAGE
            );

            //2. BATCH push yuboramiz
            notificationService.sendBatchNotification(
                    users,
                    message.getTitle(),
                    message.getBody(),
                    NotificationPathType.NEWS_MESSAGE
            );

            // timestamp ni update qilamiz — faqat muvaffaqiyatli bo'lgandan keyin
            users.forEach(u -> u.setLastInactiveNotificationAt(now));

            userRepository.saveAll(users);

            log.info("=== Scheduler muvaffaqiyatli tugadi: {} ta user ===", users.size());

        } catch (Exception e) {
            log.error("=== Scheduler xatolik: {} ===", e.getMessage(), e);
        }
    }

}
