package com.example.tinder.service;

import com.example.tinder.dto.DiscoveryResponseDTO;
import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DiscoveryService {
    ApiResponse<?> getDiscoveryUsers(Long currentUserId);
}
