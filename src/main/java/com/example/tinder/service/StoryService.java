package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.StoryMediaType;
import com.example.tinder.enums.StoryVisibility;
import org.springframework.web.multipart.MultipartFile;

public interface StoryService {
    ApiResponse<?> createStory(MultipartFile file, StoryMediaType type, StoryVisibility visibility, User user);
    ApiResponse<?> viewStory(Long storyId, User viewer);
    ApiResponse<?> getFeedStories(User currentUser);
    ApiResponse<?> getUserStories(Long targetUserId, User currentUser);
    ApiResponse<?> getMyStories(User currentUser);
    ApiResponse<?> deleteStory(Long storyId, User currentUser);
}
