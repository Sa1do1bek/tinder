package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/getUserConversations")
    public ApiResponse getUserConversations(@RequestParam Long userId) {
        return conversationService.getUserConversations(userId);
    }

    @PostMapping("/getOrCreate")
    public ApiResponse getOrCreateConversation(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        return conversationService.getOrCreateConversation(user1Id, user2Id);
    }

    @GetMapping("/find")
    public ApiResponse findConversation(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        return conversationService.findConversationBetweenUsers(user1Id, user2Id);
    }
}

