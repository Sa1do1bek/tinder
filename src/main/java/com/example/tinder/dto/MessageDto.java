package com.example.tinder.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
