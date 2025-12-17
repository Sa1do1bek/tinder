package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.ChatMessageDto;
import com.example.tinder.dto.MessageDto;
import com.example.tinder.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class MessageController {
    private final MessageService messageService;

    @MessageMapping("/chat/send")
    @SendToUser("/queue/messages")
    public ApiResponse<?> sendMessage(@Payload ChatMessageDto chatMessageDto) {
        return messageService.sendMessage(chatMessageDto);
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MessageDto>> getMessagesByConversation(
            @RequestParam Long conversationId,
            @AuthenticationPrincipal com.example.tinder.entity.User currentUser
    ) {
        List<MessageDto> messages = messageService.getMessageByConversationId(conversationId, currentUser);
        return ResponseEntity.ok(messages);
    }
}
