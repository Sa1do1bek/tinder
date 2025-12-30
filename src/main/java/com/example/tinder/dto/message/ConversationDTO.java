package com.example.tinder.dto.message;

import com.example.tinder.dto.UserDto;
import jakarta.persistence.Column;
import lombok.*;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Long id;
    private UserDto user;
    private String lastMessage;
    private Integer unreadCount;
    private Boolean isMatched;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConversationalCreateDto {
        private Long id;
        private Long user1Id;
        private Long user2Id;
    }
}
