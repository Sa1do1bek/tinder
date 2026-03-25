package com.example.tinder.scheduler;

import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerTasks {

    private final SwipeRepository swipeRepository;
    private final UserSubscriptionService userSubscriptionService;

    @Scheduled(fixedRate = 1000 * 60 * 2) //2min
    public void cleanupExpiredSwipes() {
        log.info("Cleaning expired swipes...");
        swipeRepository.deleteExpiredRejects();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void checkSubscriptions() {
        log.info("Checking subscriptions...");
        userSubscriptionService.checkSubscriptions();
    }
}
