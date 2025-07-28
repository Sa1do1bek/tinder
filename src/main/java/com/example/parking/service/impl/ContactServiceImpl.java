package com.example.parking.service.impl;

import com.example.parking.entity.ContactReason;
import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.RequestStatus;
import com.example.parking.entity.UserBot;
import com.example.parking.repository.ContactRequestRepository;
import com.example.parking.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ContactServiceImpl implements ContactService {
    
    @Autowired
    private ContactRequestRepository contactRequestRepository;
    
    private static final int MAX_REQUESTS_PER_HOUR = 3;
    private static final int MAX_REQUESTS_PER_LICENSE_PLATE_PER_HOUR = 5;
    
    @Override
    public ContactRequest createContactRequest(UserBot sender, UserBot receiver, String licensePlate,
                                               ContactReason reason, String customReason) {
        ContactRequest request = new ContactRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setLicensePlate(licensePlate);
        request.setReason(reason);
        request.setCustomReason(customReason);
        request.setStatus(RequestStatus.PENDING);
        return contactRequestRepository.save(request);
    }
    
    @Override
    public List<ContactRequest> getSentRequests(Long userId) {
        return contactRequestRepository.findBySenderChatIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public List<ContactRequest> getReceivedRequests(Long userId) {
        return contactRequestRepository.findByReceiverChatIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public boolean canSendRequest(Long senderId, String licensePlate) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        long userRequestCount = contactRequestRepository
            .countBySenderChatIdAndCreatedAtAfter(senderId, oneHourAgo);
        if (userRequestCount >= MAX_REQUESTS_PER_HOUR) {
            return false;
        }
        
        List<ContactRequest> plateRequests = contactRequestRepository
            .findByLicensePlateAndCreatedAtAfter(licensePlate, oneHourAgo);
        return plateRequests.size() < MAX_REQUESTS_PER_LICENSE_PLATE_PER_HOUR;
    }
    
    @Override
    public ContactRequest resolveRequest(Long requestId) {
        ContactRequest request = contactRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.RESOLVED);
        request.setResolvedAt(LocalDateTime.now());
        return contactRequestRepository.save(request);
    }
    
    @Override
    public Optional<ContactRequest> findById(Long requestId) {
        return contactRequestRepository.findById(requestId);
    }
}
