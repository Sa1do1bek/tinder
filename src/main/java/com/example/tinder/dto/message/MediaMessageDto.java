package com.example.tinder.dto.message;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaMessageDto {
    private Long id;
    private Long userId;
    private String cloudPath;
}
