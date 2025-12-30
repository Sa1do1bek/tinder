package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;

public interface BlockService {

    ApiResponse<?> blockUser(Long blockerId, Long blockedId);

    ApiResponse<?> unblockUser(Long blockerId, Long blockedId);

    boolean isBlocked(Long userId1, Long userId2);

    ApiResponse<?> getUserBlocks(Long userId);
}