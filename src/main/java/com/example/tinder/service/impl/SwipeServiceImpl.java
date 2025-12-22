package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Match;
import com.example.tinder.entity.Swipe;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SwipeStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.MatchRepository;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ConversationService;
import com.example.tinder.service.SwipeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SwipeServiceImpl implements SwipeService {

    private final UserRepository userRepository;
    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationService conversationService;

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

        if (status == SwipeStatus.ACCEPT) {
            boolean isMutual = swipeRepository.existsBySwiperIdAndSwipedIdAndSwipe(swipedId, swiperId, SwipeStatus.ACCEPT);

            if (isMutual) {
                boolean alreadyMatched = matchRepository.existsByUsers(swiperOpt.get(), swipedOpt.get());

                if (!alreadyMatched) {
                    Match match = Match.builder()
                            .user1(swiperOpt.get())
                            .user2(swipedOpt.get())
                            .build();
                    matchRepository.save(match);

                    conversationService.getOrCreateConversation(swiperId, swipedId);
                }

                Map<String, Object> matchNotification = new HashMap<>();
                matchNotification.put("type", "MATCH");
                matchNotification.put("fromUserId", swiperOpt.get().getId());
                matchNotification.put("fromFullName", swiperOpt.get().getFullname());
                matchNotification.put("message", "You have a new match! You can now start chatting.");

                messagingTemplate.convertAndSend(
                        "/topic/notification/" + swipedOpt.get().getId(),
                        matchNotification
                );

                messagingTemplate.convertAndSend(
                        "/topic/notification/" + swiperOpt.get().getId(),
                        matchNotification
                );
                return ApiResponse.success("Match found! You can now start chatting.");
            }
        }

        return ApiResponse.success("Swipe recorded.");
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

        return ApiResponse.success(result, "SUCCESS");
    }
}
