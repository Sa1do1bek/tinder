package com.example.tinder.dto.message;

import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;
}
