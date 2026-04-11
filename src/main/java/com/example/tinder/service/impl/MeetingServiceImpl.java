package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.meeting.responses.AcceptResponse;
import com.example.tinder.dto.meeting.responses.CancellationResponse;
import com.example.tinder.dto.meeting.responses.InvitationResponse;
import com.example.tinder.dto.meeting.responses.RejectionResponse;
import com.example.tinder.entity.Meeting;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.MeetingStatus;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.MeetingComponentsMapper;
import com.example.tinder.repository.MeetingRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.MeetingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingComponentsMapper meetingComponentsMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @Override
    public ApiResponse<?> createMeeting(String username, Long receiverId) {
        User inviter = userRepository.findByUsername(username)
                .orElseThrow(() ->  RestException.restThrow(ErrorType.USER_NOT_FOUND));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() ->  RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (Objects.equals(inviter.getId(), receiver.getId()))
            throw RestException.restThrow(ErrorType.SAME_USER_CALL);

        Meeting meeting = new Meeting();
        meeting.setInviter(inviter);
        meeting.setReceiver(receiver);

        UUID roomId = UUID.randomUUID();
        meeting.setRoomId(roomId);

        InvitationResponse responseForReceiver = new InvitationResponse(
                meetingComponentsMapper.toUserInvitationDto(inviter), "An invitation for a meeting!");

        meetingRepository.save(meeting);

        log.info("Subscribe invite for receiver called: username={}", username);

        this.sendToUser(receiver.getUsername(), responseForReceiver);

        return ApiResponse.success(SuccessType.INVITATION_SENT_SUCCESSFULLY);
    }

    @Transactional
    @Override
    public ApiResponse<?> acceptMeeting(String accepterUsername) {
        Meeting meeting = this.getByReceiverUsername(accepterUsername);

        if (meeting.getStatus() != MeetingStatus.PENDING)
            throw RestException.restThrow(ErrorType.MEETING_STATUS_NOT_SUITABLE);

        AcceptResponse response = new AcceptResponse(meeting.getRoomId().toString(), "Meeting accepted!");

        meeting.setStartedAt(LocalDateTime.now());
        meeting.setStatus(MeetingStatus.ACCEPTED);

        log.info("Subscribe accept for receiver called: username={}", meeting.getInviter().getUsername());
        log.info("Subscribe accept for receiver called: username={}", meeting.getReceiver().getUsername());

        this.sendToUser(meeting.getInviter().getUsername(), response);
        this.sendToUser(meeting.getReceiver().getUsername(), response);

        meetingRepository.save(meeting);

        return ApiResponse.success("Meeting has been accepted and its roomId has been sent!");
    }

    @Transactional
    @Override
    public ApiResponse<?> rejectMeeting(String rejecterUsername) {
        Meeting meeting = this.getByReceiverUsername(rejecterUsername);

        if (meeting.getStatus() != MeetingStatus.PENDING)
            throw RestException.restThrow(ErrorType.MEETING_STATUS_NOT_SUITABLE);

        meeting.setStatus(MeetingStatus.REJECTED);

        RejectionResponse response = new RejectionResponse("A meeting invitation has been rejected!");

        this.sendToUser(meeting.getInviter().getUsername(), response);
        meetingRepository.save(meeting);

        return ApiResponse.success("Meeting has been rejected!");
    }

    @Transactional
    @Override
    public ApiResponse<?> cancelMeeting(String cancellerUsername) {
        Meeting meeting = this.getByInviterUsername(cancellerUsername);

        if (meeting.getStatus() != MeetingStatus.ACCEPTED)
            throw RestException.restThrow(ErrorType.MEETING_STATUS_NOT_SUITABLE);

        meeting.setStatus(MeetingStatus.CANCELLED);

        CancellationResponse response = new CancellationResponse("A meeting invitation has been cancelled!");

        this.sendToUser(meeting.getReceiver().getUsername(), response);
        meetingRepository.save(meeting);

        return ApiResponse.success("Meeting has been cancelled!");
    }

    @Transactional
    @Override
    public ApiResponse<?> endMeeting(String enderUsername) {
        Meeting meeting = this.getActiveMeetingForUser(enderUsername);

        if (meeting.getStatus() != MeetingStatus.ACCEPTED)
            throw RestException.restThrow(ErrorType.MEETING_STATUS_NOT_SUITABLE);

        meeting.setStatus(MeetingStatus.ENDED);

        CancellationResponse response = new CancellationResponse("A meeting invitation has been ended!");

        this.sendToUser(meeting.getInviter().getUsername(), response);
        this.sendToUser(meeting.getReceiver().getUsername(), response);
        meetingRepository.save(meeting);

        return ApiResponse.success("Meeting has been ended!");
    }

    /** Helper Methods **/

    private Meeting getByReceiverUsername(String receiverUsername) {
        return meetingRepository.findFirstByReceiver_UsernameAndStatusOrderByCreatedAtDesc(
                        receiverUsername, MeetingStatus.PENDING)
                .orElseThrow(() -> RestException.restThrow(ErrorType.MEETING_NOT_FOUND));
    }

    private Meeting getByInviterUsername(String inviterUsername) {
        return meetingRepository.findFirstByInviter_UsernameAndStatusOrderByCreatedAtDesc(
                        inviterUsername, MeetingStatus.ACCEPTED)
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.MEETING_NOT_FOUND));
    }

    private Meeting getActiveMeetingForUser(String username) {
        return meetingRepository.findActiveMeeting(username)
                .orElseThrow(() ->
                        RestException.restThrow(ErrorType.MEETING_NOT_FOUND));
    }

    private void sendToUser(String username, Object payload) {
        log.info(username, payload);
        messagingTemplate.convertAndSendToUser(
                username,
                "/queue/feedback",
                payload
        );
    }
}
