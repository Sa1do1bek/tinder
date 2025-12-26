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
    private Long receiverId;
    private Long conversationId;
    private String SenderName;
    private String message;
    private Boolean isRead;
    private Boolean isLiked;
    private Boolean edited;
    private LocalDateTime createdAt;
    private Boolean deletedForSender;
    private Boolean deletedForRecipient;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageUpdateDto {
        private Long id;
        private String message;
    }


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageCreateDto {
        private Long senderId;
        private Long receiverId;
        private Long conversationId;
        private String message;
    }
}
