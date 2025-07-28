package com.example.parking.repository;

import com.example.parking.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {
    List<ContactRequest> findBySenderChatIdOrderByCreatedAtDesc(Long senderChatId);
    List<ContactRequest> findByReceiverChatIdOrderByCreatedAtDesc(Long receiverChatId);
    List<ContactRequest> findByLicensePlateAndCreatedAtAfter(String licensePlate, LocalDateTime after);
    long countBySenderChatIdAndCreatedAtAfter(Long senderChatId, LocalDateTime after);
}
