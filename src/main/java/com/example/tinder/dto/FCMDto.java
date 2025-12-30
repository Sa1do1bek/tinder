package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMDto {
    private UserDto user;
    private String fcmToken;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateFCM {
        private String fcmToken;
    }
}