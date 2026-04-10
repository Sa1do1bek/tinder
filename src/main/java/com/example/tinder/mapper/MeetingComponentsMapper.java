package com.example.tinder.mapper;

import com.example.tinder.dto.UserInvitationDto;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MeetingComponentsMapper {

    public UserInvitationDto toUserInvitationDto(User inviter) {
        return new UserInvitationDto(
                inviter.getId(),
                inviter.getUsername(),
                inviter.getProfilePic()
        );
    }
}
