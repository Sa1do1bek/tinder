package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.FCM;
import com.example.tinder.entity.Match;
import com.example.tinder.entity.Swipe;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.NotificationPathType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.SwipeStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.ConversationMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.NotificationService;
import com.example.tinder.service.SwipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwipeServiceImpl implements SwipeService {

    private final UserRepository userRepository;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;
    private final ConversationRepository conversationRepository;
    private final FCMRepository fcmRepository;

    @Override
    public ApiResponse<?> swipe(Long swiperId, Long swipedId, SwipeStatus status) {
        if (swiperId.equals(swipedId)) {
            throw RestException.restThrow(ErrorType.YOU_CANNOT_SWIPE_YOURSELF);
        }

        Optional<User> swipedOpt = userRepository.findById(swipedId);
        Optional<User> swiperOpt = userRepository.findById(swiperId);

        if (swiperOpt.isEmpty() || swipedOpt.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User swiper = swiperOpt.get();
        User swiped = swipedOpt.get();

        if (!swiper.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }

        if (!swiped.isActive()) {
            throw RestException.restThrow(ErrorType.THIS_USER_IS_NO_LONGER_AVAILABLE);
        }


        Optional<Swipe> optionalSwipe = swipeRepository.findBySwiperIdAndSwipedId(swiperId, swipedId);
        Swipe swipe;

        if (optionalSwipe.isPresent()) {
            swipe = optionalSwipe.get();
            swipe.setSwipe(status);
        } else {
            swipe = Swipe.builder()
                    .swiper(swiperOpt.get())
                    .swiped(swipedOpt.get())
                    .swipe(status)
                    .build();
        }

        swipeRepository.save(swipe);


        if (status == SwipeStatus.LIKE || status == SwipeStatus.SUPERLIKE) {
            boolean isMutual = swipeRepository.existsBySwiperIdAndSwipedIdAndSwipeAndVisibilityTrue(swipedId, swiperId, SwipeStatus.LIKE);
            boolean isMutual1 = swipeRepository.existsBySwiperIdAndSwipedIdAndSwipeAndVisibilityTrue(swipedId, swiperId, SwipeStatus.SUPERLIKE);
            if (isMutual || isMutual1) {
                boolean alreadyMatched = matchRepository.existsByUsers(swiperOpt.get(), swipedOpt.get());

                if (!alreadyMatched) {
                    Match match = Match.builder()
                            .user1(swiperOpt.get())
                            .user2(swipedOpt.get())
                            .build();
                    matchRepository.save(match);
                }
                findConversationBetween(swiperId, swipedId);

                Map<String, Object> matchNotification = new HashMap<>();
                matchNotification.put("type", "MATCH");
                matchNotification.put("fromUserId", swiperOpt.get().getId());
                matchNotification.put("fromFullName", swiperOpt.get().getFullname());
                matchNotification.put("message", "You have a new match! You can now start chatting.");

                fcmRepository.findByUserIdAndVisibilityTrue(swipedId).ifPresent(swipedFCM -> notificationService.sendNotification(
                        swiped.getId(),
                        "TINDER!",
                        swiper.getFullname() + " sizga " + status.name() + " bosdi." + "\n" +
                                " Chat yaratildi! siz suhbat qilishingiz mumkin.",
                        NotificationPathType.NEWS_MESSAGE
                ));

                fcmRepository.findByUserIdAndVisibilityTrue(swiperId).ifPresent(swipedFCM -> notificationService.sendNotification(
                        swiper.getId(),
                        swiped.getFullname() + " bilan chat yaratildi!",
                        "Siz suhbat qilishingiz mumkin.",
                        NotificationPathType.NEWS_MESSAGE
                ));

                messagingTemplate.convertAndSend(
                        "/topic/notification/" + swipedOpt.get().getId(),
                        matchNotification
                );

                messagingTemplate.convertAndSend(
                        "/topic/notification/" + swiperOpt.get().getId(),
                        matchNotification
                );

                swipeRepository.hideSwipes(swiperId, swipedId);
                swipeRepository.hideSwipes(swipedId, swiperId);
                return ApiResponse.success(SuccessType.MATCH_FOUND_YOU_CAN_START_CHAT);
            }

            notificationService.sendNotification(
                    swiped.getId(),
                    "TINDER!",
                    swiper.getFullname() + " sizga " + status.name() + " bosdi.",
                    NotificationPathType.NEWS_MESSAGE
            );
        }

        return ApiResponse.success(SuccessType.SWIPE_RECORDED);
    }


    private void findConversationBetween(Long user1Id, Long user2Id) {
        conversationRepository.findBetweenUsers(user1Id, user2Id);
    }


    @Override
    @Transactional
    public ApiResponse<?> getSwipesByUser(Long userId) {
        List<Swipe> swipeList = swipeRepository.findAllBySwiper_Id(userId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Swipe swipe : swipeList) {
            Map<String, Object> data = new HashMap<>();
            data.put("swipedUserId", swipe.getSwiped().getId());
            data.put("swipedFullName", swipe.getSwiped().getFullname());
            data.put("status", swipe.getSwipe());
            result.add(data);
        }

        return ApiResponse.success(result, SuccessType.SWIPES_FETCHED);
    }



    @Scheduled(fixedRate = 1000*60*2)
    @Transactional
    public void cleanupExpiredSwipes() {
        swipeRepository.deleteExpiredRejects();
    }

}
