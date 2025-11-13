package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.UserLog;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface UserLogService {

    UserLog save(UserLog userLog);
    ApiResponse getAllUserLogs(Long userId, String username, String action, String endpoint, String ipAddress, Pageable pageable);
}
