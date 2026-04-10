package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Component;

@Component
public interface MeetingService {

    ApiResponse<?> createMeeting(String inviterUsername, Long receiverId);
    ApiResponse<?> acceptMeeting(String accepterUsername);
    ApiResponse<?> rejectMeeting(String rejecterUsername);
    ApiResponse<?> cancelMeeting(String cancellerUsername);
    ApiResponse<?> endMeeting(String enderUsername);
}
