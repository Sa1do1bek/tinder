package com.example.tinder.dto.story;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryViewerDto {
    private Long userId;
    private String username;
    private String profilePic;
    private LocalDateTime viewedAt;
}
