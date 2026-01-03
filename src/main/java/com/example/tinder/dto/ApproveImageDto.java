package com.example.tinder.dto;


import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApproveImageDto {
    private Long id;
    private Long userId;
    private String cloudPath;
}
