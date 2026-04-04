package com.example.tinder.dto.story;

import com.example.tinder.enums.StoryMediaType;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryResponseDto {
    private Long id;
    private String mediaUrl;
    private StoryMediaType mediaType;
    private LocalDateTime createdAt;
    private boolean isViewed;
    private Integer viewCount;
    private List<StoryViewerDto> viewers;
}
