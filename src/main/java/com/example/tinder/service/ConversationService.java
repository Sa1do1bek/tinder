package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ConversationService {

    ApiResponse<?> getUserConversations(Long userId);

    ApiResponse<?> createConversation(Long currentUserId, Long user2Id);

    ApiResponse<?> findConversationBetweenUsers(Long currentUserId, Long user2Id);

    ApiResponse<?> deleteConversationForMe(Long currentUserId, Long user2Id);

    ApiResponse<?> deleteConversation(Long currentUserId, Long user2Id);

    ApiResponse<?> getUserAllUnreadCounts(Long currentUserId);

    boolean isMatchExists(Long user1Id, Long user2Id);
}
