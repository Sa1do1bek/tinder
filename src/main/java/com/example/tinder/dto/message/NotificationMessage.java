package com.example.tinder.dto.message;

import com.example.tinder.dto.NotificationDto;
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
