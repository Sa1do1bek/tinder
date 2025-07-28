package com.example.parking.service;



import com.example.parking.entity.ContactReason;
import com.example.parking.entity.ContactRequest;
import com.example.parking.entity.UserBot;

import java.util.List;
import java.util.Optional;

public interface ContactService {
    ContactRequest createContactRequest(UserBot sender, UserBot receiver, String licensePlate,
                                        ContactReason reason, String customReason);
    List<ContactRequest> getSentRequests(Long userId);
    List<ContactRequest> getReceivedRequests(Long userId);
    boolean canSendRequest(Long senderId, String licensePlate);
    ContactRequest resolveRequest(Long requestId);
    Optional<ContactRequest> findById(Long requestId);
}
