package com.example.tinder.service;
import com.example.tinder.dto.base.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public interface DiscoveryService {
    ApiResponse<?> getDiscoveryUsers(Long currentUserId, Pageable pageable);

}
