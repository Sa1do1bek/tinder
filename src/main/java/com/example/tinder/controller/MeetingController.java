package com.example.tinder.controller;

import com.example.tinder.dto.meeting.requests.InviteDto;
import com.example.tinder.dto.message.MessageDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/meeting")
public class MeetingController {

    private final MeetingService meetingService;

    @MessageMapping("/invite")
    public void invite(@Payload InviteDto inviteDto,
                                 @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username == null)
            return;

        meetingService.createMeeting(username, inviteDto.receiverId());
    }

    @MessageMapping("/accept")
    public void accept(@Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username == null)
            return;

        meetingService.acceptMeeting(username);
    }

    @MessageMapping("/reject")
    public void reject(@Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username == null)
            return;

        meetingService.rejectMeeting(username);
    }

    @MessageMapping("/cancel")
    public void cancel(@Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username == null)
            return;

        meetingService.cancelMeeting(username);
    }

    @MessageMapping("/end")
    public void end(@Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username == null)
            return;

        meetingService.endMeeting(username);
    }

}
