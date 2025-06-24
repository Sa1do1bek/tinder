package com.example.tinder.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConversationDTO {
    private Long id;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;
}
