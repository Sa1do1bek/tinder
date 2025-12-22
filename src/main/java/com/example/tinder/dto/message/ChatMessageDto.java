package com.example.tinder.dto.message;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
  private Long senderId;
  private Long receiverId;
  private String message;
  private Long conversationId;
}
