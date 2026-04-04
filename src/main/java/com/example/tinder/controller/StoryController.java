package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.StoryMediaType;
import com.example.tinder.enums.StoryVisibility;
import com.example.tinder.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<?> createStory(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") StoryMediaType type,
            @RequestParam(value = "visibility", defaultValue = "PUBLIC") StoryVisibility visibility,
            @AuthenticationPrincipal User user) {

        return storyService.createStory(file, type, visibility, user);
    }

    @PostMapping("/{storyId}/view")
    public ApiResponse<?> viewStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal User user) {

        return storyService.viewStory(storyId, user);
    }

    @GetMapping("/feed")
    public ApiResponse<?> getFeedStories(@AuthenticationPrincipal User user) {
        return storyService.getFeedStories(user);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<?> getUserStories(
            @PathVariable Long userId,
            @AuthenticationPrincipal User user) {

        return storyService.getUserStories(userId, user);
    }

    @GetMapping("/me")
    public ApiResponse<?> getMyStories(@AuthenticationPrincipal User user) {
        return storyService.getMyStories(user);
    }

    @DeleteMapping("/{storyId}")
    public ApiResponse<?> deleteStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal User user) {

        return storyService.deleteStory(storyId, user);
    }
}
