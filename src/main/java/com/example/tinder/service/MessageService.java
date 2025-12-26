package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public interface MessageService {

    ApiResponse<?> sendMessage(MessageDto.MessageCreateDto chatMessageDto);

    Page<MessageDto> getMessagesByConversationId(Long conversationId, User currentUser, Pageable pageable);

    ApiResponse<?> deleteMessageForMe(Long messageId, User currentUser);
}
