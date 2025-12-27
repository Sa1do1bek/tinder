package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.MessageService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class MessageController {
    private final MessageService messageService;
    private final UserRepository userRepository;

    @ResponseBody
    @PostMapping("/sendMessage")
    public ApiResponse<?> sendMessage(
            @RequestBody MessageDto.MessageCreateDto chatMessageDto,
            @CurrentUser User currentUser) {

        // Security check
        if (!chatMessageDto.getSenderId().equals(currentUser.getId())) {
            throw new RuntimeException("UNAUTHORIZED, You are not allowed to send message to this user");
        }

        return messageService.sendMessage(chatMessageDto);
    }

    @ResponseBody
    @GetMapping("/messages")
    public ApiResponse<?> getMessagesByConversation(
            @CurrentUser User currentUser,
            @RequestParam Long conversationId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        return messageService.getMessagesByConversationId(conversationId, currentUser, pageable);
    }

    @ResponseBody
    @DeleteMapping("/deleteForMe")
    public ApiResponse<?> deleteForMe(
            @RequestParam Long messageId,
            @CurrentUser User currentUser) {
        return messageService.deleteMessageForMe(messageId, currentUser);
    }

    @ResponseBody
    @DeleteMapping("/deleteForAll")
    public ApiResponse<?> deleteForAll(
            @RequestParam Long messageId,
            @CurrentUser User currentUser) {
        return messageService.deleteMessageForAll(messageId, currentUser);
    }

//    markMessageAsRead
    @ResponseBody
    @PostMapping("/markAsRead")
    public ApiResponse<?> markAsRead(
            @RequestParam Long conversationId,
            @CurrentUser User currentUser) {
        return messageService.markMessageAsRead(conversationId, currentUser);
    }




    @ResponseBody
    @PutMapping("/updateMessage")
    public ApiResponse<?> updateMessage(
            MessageDto.MessageUpdateDto dto,
            @CurrentUser User currentUser) {
        return messageService.updateMessage(dto, currentUser);
    }



    @MessageMapping("/chat/send")
    public void sendViaWebSocket(@Payload MessageDto.MessageCreateDto chatMessageDto,
                                 @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        // WebSocket session'dan username olish
        String username = (String) sessionAttributes.get("username");
        if (username == null) {
            log.error("❌ WebSocket send failed: username not found in session");
            return;
        }

        // Username orqali user topish
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() ->  RestException.restThrow(ErrorType.USER_NOT_FOUND));
        if (!chatMessageDto.getSenderId().equals(sender.getId())) {
            log.error("❌ Unauthorized WebSocket send: username={}, claimedSenderId={}",
                    username, chatMessageDto.getSenderId());
            return;
        }
        messageService.sendMessage(chatMessageDto);
        log.info("📤 Message sent via WebSocket: from={}", username);
    }
}
