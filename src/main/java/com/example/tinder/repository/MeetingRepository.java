package com.example.tinder.repository;

import com.example.tinder.entity.Meeting;
import com.example.tinder.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findFirstByReceiver_UsernameAndStatusOrderByCreatedAtDesc(String receiverUsername, MeetingStatus meetingStatus);
    Optional<Meeting> findFirstByInviter_UsernameAndStatusOrderByCreatedAtDesc(String inviterUsername, MeetingStatus meetingStatus);

    @Query("SELECT m FROM Meeting m WHERE (m.inviter.username = :user OR m.receiver.username = :user) " +
            "AND m.status = 'ACCEPTED' ORDER BY m.createdAt DESC LIMIT 1")
    Optional<Meeting> findActiveMeeting(@Param("user") String username);
}
