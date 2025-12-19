package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Long fromUserId;
    private Long toUserId;
    private String message;
}
