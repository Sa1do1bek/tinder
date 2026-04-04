package com.example.tinder.entity;

import com.example.tinder.enums.StoryMediaType;
import com.example.tinder.enums.StoryVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stories")
public class Story extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryMediaType mediaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoryVisibility storyVisibility = StoryVisibility.PUBLIC;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(columnDefinition = "int default 0")
    private int viewCount = 0;
}
