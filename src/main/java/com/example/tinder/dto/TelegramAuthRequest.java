package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramAuthRequest {
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
}