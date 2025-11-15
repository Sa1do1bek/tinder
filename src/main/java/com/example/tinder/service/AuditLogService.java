package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {

    ApiResponse getAllAuditLogs(String action, String username, Long userId, String role, String ipAddress, String endPoint, String sourceType, String entityName, Long entityId, LocalDateTime from, LocalDateTime to, Pageable pageable);

}
