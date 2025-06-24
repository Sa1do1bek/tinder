package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.Conversation;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ConversationService {

    ApiResponse getUserConversations(Long userId);

    ApiResponse getOrCreateConversation(Long user1Id, Long user2Id);

    ApiResponse findConversationBetweenUsers(Long user1Id, Long user2Id);
    boolean isMatchExists(Long user1Id, Long user2Id);
    Optional<Conversation> getOrCreateConversationEntity(Long user1Id, Long user2Id);
}
