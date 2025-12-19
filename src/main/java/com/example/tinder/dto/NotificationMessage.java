package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private Long fromUserId;
    private String message;

    public NotificationMessage(NotificationDto dto) {
        this.fromUserId = dto.getFromUserId();
        this.message = dto.getMessage();
    }
}
