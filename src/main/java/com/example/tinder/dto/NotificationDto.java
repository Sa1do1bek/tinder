package com.example.tinder.dto;

import com.example.tinder.enums.NotificationPathType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private NotificationPathType pathType;
}
