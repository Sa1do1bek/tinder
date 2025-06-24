package com.example.tinder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationMessage {
    private Long fromUserId;
    private String message;

    public NotificationMessage(NotificationDto dto) {
        this.fromUserId = dto.getFromUserId();
        this.message = dto.getMessage();
    }
}
