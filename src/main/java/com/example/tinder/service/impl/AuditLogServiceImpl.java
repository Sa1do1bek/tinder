package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.AuditLog;
import com.example.tinder.repository.AuditLogRepository;
import com.example.tinder.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public ApiResponse getAllAuditLogs(String action, String username, Long userId, String role, String ipAddress, String endPoint, String sourceType, String entityName, Long entityId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAllAuditLogs(
                action, username, userId, role, ipAddress,
                endPoint, sourceType, entityName, entityId, from, to, pageable
        );
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .data(logs)
                .build();
    }

}
