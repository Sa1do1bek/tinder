package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.ConversationService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping("/find")
    public ApiResponse<?> findConversation(@CurrentUser User user,
                                           @RequestParam Long user2Id) {
        return conversationService.findConversationBetweenUsers(user.getId(), user2Id);
    }

    @PostMapping("/create")
    public ApiResponse<?> getOrCreateConversation(@CurrentUser User user, @RequestParam Long user2Id) {
        return conversationService.createConversation(user.getId(), user2Id);
    }

    @PostMapping("/createDirectly")
    public ApiResponse<?> getOrCreateConversationDirectly(@CurrentUser User user, @RequestParam Long user2Id) {
        return conversationService.createConversationDirectly(user.getId(), user2Id);
    }

    @PostMapping("/markAsMatch")
    public ApiResponse<?> markAsMatchConversation(@RequestParam("conversationId") Long conversationId) {
        return conversationService.markAsMatchConversation(conversationId);
    }

    @DeleteMapping("/deleteForMe")
    public ApiResponse<?> deleteForMe(@CurrentUser User user,
                                      @RequestParam Long user2Id) {
        return conversationService.deleteConversationForMe(user.getId(), user2Id);
    }

    @DeleteMapping("/deleteForAll")
    public ApiResponse<?> deleteForAll(@CurrentUser User user,
                                       @RequestParam Long user2Id) {
        return conversationService.deleteConversation(user.getId(), user2Id);
    }

    @GetMapping("/FindBetweenUsers")
    public ApiResponse<?> FindBetweenUsers(@CurrentUser User user,
                                           @RequestParam Long user2Id) {
        return conversationService.findConversationBetweenUsers(user.getId(), user2Id);
    }

    @GetMapping("/getUserConversations")
    public ApiResponse<?> getUserConversations(@CurrentUser User user) {
        return conversationService.getUserConversations(user.getId());
    }
}
