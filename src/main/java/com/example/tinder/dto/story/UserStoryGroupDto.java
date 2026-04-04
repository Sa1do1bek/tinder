package com.example.tinder.dto.story;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserStoryGroupDto {
    private Long userId;
    private String username;
    private String profilePic;
    private List<StoryResponseDto> stories;
    private boolean allSeen;
}
