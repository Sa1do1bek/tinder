package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfilePictureDto {
    private Long id;
    private String cloudPath;

}
