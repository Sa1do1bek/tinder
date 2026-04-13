package com.example.tinder.service;

import org.springframework.stereotype.Component;

@Component
public interface MeetingService {

    void createMeeting(String inviterUsername, Long receiverId);
    void acceptMeeting(String accepterUsername);
    void rejectMeeting(String rejecterUsername);
    void cancelMeeting(String cancellerUsername);
    void endMeeting(String enderUsername);
}
