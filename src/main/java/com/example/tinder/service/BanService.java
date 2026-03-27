package com.example.tinder.service;

import com.example.tinder.dto.ban.BanUserRequest;
import com.example.tinder.dto.base.ApiResponse;

public interface BanService {
    ApiResponse<?> banUser(Long userId, BanUserRequest banUserRequest);
    ApiResponse<?> unBan(Long userId);
}
