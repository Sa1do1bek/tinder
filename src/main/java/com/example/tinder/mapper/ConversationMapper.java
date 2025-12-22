package com.example.tinder.mapper;

import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationMapper {

    public ConversationDTO toDto(Conversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .user1Id(conversation.getUser1().getId())
                .user1Name(conversation.getUser1().getFullname())
                .user2Id(conversation.getUser2().getId())
                .user2Name(conversation.getUser2().getFullname())
                .build();
    }

}
