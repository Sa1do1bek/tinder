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
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @MessageMapping("/invite")
    public void invite(@Payload InviteDto inviteDto, Principal principal) {

        String sender = principal.getName();

        log.info("Invite called: sender={}", sender);

        meetingService.createMeeting(sender, inviteDto.receiverId());
    }

    @MessageMapping("/accept")
    public void accept(Principal principal) {
        String sender = principal.getName();

        log.info("Accept called: sender={}", sender);

        meetingService.acceptMeeting(sender);
    }

    @MessageMapping("/reject")
    public void reject(Principal principal) {
        String sender = principal.getName();

        log.info("Reject called: sender={}", sender);

        meetingService.rejectMeeting(sender);
    }

    @MessageMapping("/cancel")
    public void cancel(Principal principal) {
        String sender = principal.getName();

        log.info("Cancel called: sender={}", sender);

        meetingService.cancelMeeting(sender);
    }

    @MessageMapping("/end")
    public void end(Principal principal) {
        String sender = principal.getName();

        log.info("End called: sender={}", sender);

        meetingService.endMeeting(sender);
    }

}
