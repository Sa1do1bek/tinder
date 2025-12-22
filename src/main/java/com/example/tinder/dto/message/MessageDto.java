package com.example.tinder.dto.message;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime timestamp;
    private Boolean isRead;
}
