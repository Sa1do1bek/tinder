package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.ChatMessageDto;
import com.example.tinder.dto.MessageDto;
import com.example.tinder.entity.Conversation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface MessageService {
    Optional<Conversation> findConversationEntity(Long user1Id, Long user2Id);
    ApiResponse sendMessage(ChatMessageDto chatMessageDto);

    List<MessageDto> getMessageByConversationId(Long conversationId);
}
