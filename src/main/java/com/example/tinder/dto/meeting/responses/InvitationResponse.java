package com.example.tinder.dto.meeting.responses;


import com.example.tinder.dto.UserInvitationDto;

public record InvitationResponse(
        UserInvitationDto userInvitationDto,
        String message
) {
}
