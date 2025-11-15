package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.UserLog;
import com.example.tinder.repository.UserLogRepository;
import com.example.tinder.service.UserLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserLogServiceImpl implements UserLogService {

    private final UserLogRepository userLogRepository;

    @Override
    @Transactional
    public UserLog save(UserLog userLog) {
        return userLogRepository.save(userLog);
    }

    @Override
    public ApiResponse getAllUserLogs(Long userId, String username, String action, String endpoint, String ipAddress, Pageable pageable) {
        Page<UserLog> allByFilter = userLogRepository.findAllByFilter(userId, username, action, endpoint, ipAddress, pageable);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Success")
                .data(allByFilter)
                .build();
    }

}
