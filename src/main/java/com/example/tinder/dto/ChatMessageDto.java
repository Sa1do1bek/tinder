package com.example.tinder.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
  private Long receiverId;
  private String message;
  private Long conversationId;
}
