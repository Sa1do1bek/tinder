package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/getUserConversations")
    public ApiResponse getUserConversations(@AuthenticationPrincipal User user) {
        return conversationService.getUserConversations(user.getId());
    }

    @PostMapping("/getOrCreate")
    public ApiResponse getOrCreateConversation(@AuthenticationPrincipal User user, @RequestParam Long user2Id) {
        return conversationService.getOrCreateConversation(user.getId(), user2Id);
    }

    @GetMapping("/find")
    public ApiResponse findConversation(@AuthenticationPrincipal User user, @RequestParam Long user2Id) {
        return conversationService.findConversationBetweenUsers(user.getId(), user2Id);
    }
}

