package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import com.example.tinder.service.MessageService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class MessageController {
    private final MessageService messageService;

    @PostMapping("sendMessage")
    @MessageMapping("/chat/send")
    @SendToUser("/queue/messages")
    public ApiResponse<?> sendMessage(@Payload MessageDto.MessageCreateDto  chatMessageDto) {
        return messageService.sendMessage(chatMessageDto);
    }

    @GetMapping("/messages")
    public ApiResponse<?> getMessagesByConversation(
            @CurrentUser User currentUser,
            @RequestParam Long conversationId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        return ApiResponse.success(messageService.getMessagesByConversationId(conversationId, currentUser, pageable), "SUCCESS");
    }


    @DeleteMapping("/deleteForMe")
    public ApiResponse<?> getConversations(
            @RequestParam Long messageId,
            @CurrentUser User currentUser) {
        return ApiResponse.success(messageService.deleteMessageForMe(messageId, currentUser), "SUCCESS");
    }

}
