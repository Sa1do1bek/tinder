package com.example.tinder.mapper;

import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationMapper {
    private final UserMapper userMapper;

    public ConversationDTO toDto(Conversation conversation, User user) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .user(userMapper.toDto(user))
                .unreadCount(conversation.getUnreadCount())
                .build();
    }

}
