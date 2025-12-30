package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public interface ConversationService {

    ApiResponse<?> getUserConversations(Long userId);

    ApiResponse<?> createConversation(Long currentUserId, Long user2Id);

    ApiResponse<?> createConversationDirectly(Long user1Id, Long user2Id);

    ApiResponse<?> markAsMatchConversation(Long conversationId);

    ApiResponse<?> findConversationBetweenUsers(Long currentUserId, Long user2Id);

    ApiResponse<?> deleteConversationForMe(Long currentUserId, Long user2Id);

    ApiResponse<?> deleteConversation(Long currentUserId, Long user2Id);

    ApiResponse<?> getUserAllUnreadCounts(Long currentUserId);

    boolean isMatchExists(Long user1Id, Long user2Id);
}
