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

    @Scheduled(cron = "*/30 * * * * ?", zone = "Asia/Tashkent") // har kuni kechga soat 10:00PM
    public void sendInactiveUserNotifications() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveDaysAgo = now.minusDays(5);
        LocalDateTime twoDaysAgo = now.minusDays(2);

        List<User> users = userRepository.findUsersForInactiveNotification(
                fiveDaysAgo,
                twoDaysAgo
        );

        log.info("Inactive users count: {}", users.size());

        if (users.isEmpty()) return;

        NotificationMessages message = getRandomMessage();


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

            //3. timestamp update
            users.forEach(u -> u.setLastInactiveNotificationAt(now));

            userRepository.saveAll(users);

        } catch (Exception e) {
            log.error("Scheduler error", e);
        }
    }

}
