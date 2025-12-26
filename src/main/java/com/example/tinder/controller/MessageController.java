package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import com.example.tinder.service.MessageService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("/chat")
public class MessageController {
    private final MessageService messageService;

    @ResponseBody
    @PostMapping("/sendMessage")
    public ApiResponse<?> sendMessage(@RequestBody MessageDto.MessageCreateDto  chatMessageDto) {
       return messageService.sendMessage(chatMessageDto);
    }


    @ResponseBody
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

    @ResponseBody
    @DeleteMapping("/deleteForMe")
    public ApiResponse<?> deleteForMe(
            @RequestParam Long messageId,
            @CurrentUser User currentUser) {
        return ApiResponse.success(messageService.deleteMessageForMe(messageId, currentUser), "SUCCESS");
    }



    @ResponseBody
    @DeleteMapping("/deleteForAll")
    public ApiResponse<?> deleteForAll(
            @RequestParam Long messageId,
            @CurrentUser User currentUser) {
        return ApiResponse.success(messageService.deleteMessageForAll(messageId, currentUser), "SUCCESS");
    }


    @MessageMapping("/chat/send")
    @SendToUser("/queue/messages")
    public void sendWithWebsocket(@Payload MessageDto.MessageCreateDto  chatMessageDto) {
        messageService.sendMessage(chatMessageDto);
    }
}
