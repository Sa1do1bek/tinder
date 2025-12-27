package com.example.tinder.mapper;

import com.example.tinder.config.WebSocketEventListener;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.message.ConversationDTO;
import com.example.tinder.entity.Conversation;
import com.example.tinder.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConversationMapper {
    private final WebSocketEventListener webSocketEventListener;
    private final UserMapper userMapper;

    public ConversationDTO toDto(Conversation conversation, User user, String lastMessage) {

        User otherUser = conversation.getUser1().getId().equals(user.getId())
                ? conversation.getUser2()
                : conversation.getUser1();

        UserDto userDto = userMapper.toDto(otherUser);

        // 🔥 REAL-TIME ONLINE STATUS
        boolean isOnline = webSocketEventListener
                .isUserOnline(otherUser.getUsername());

        userDto.setIsOnline(isOnline);

        if (!isOnline) {
            userDto.setLastSeen(otherUser.getLastSeen());
        } else {
            userDto.setLastSeen(null);
        }

        return ConversationDTO.builder()
                .id(conversation.getId())
                .user(userMapper.toDto(user))
                .lastMessage(lastMessage)
                .unreadCount(conversation.getUnreadCount())
                .build();
    }

}
