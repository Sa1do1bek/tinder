package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.AuditLogService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/audit/log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/getAll")
    public ApiResponse getAll(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String action,
                              @RequestParam(required = false) LocalDateTime from,
                              @RequestParam(required = false) LocalDateTime to,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false) String ipAddress,
                              @RequestParam(required = false) String endPoint,
                              @RequestParam(required = false) String sourceType,
                              @RequestParam(required = false) String entityName,
                              @RequestParam(required = false) Long entityId) {
        return this.auditLogService.getAllAuditLogs(
                action,
                username,
                userId,
                role,
                ipAddress,
                endPoint,
                sourceType,
                entityName,
                entityId,
                from,
                to,
                PageRequest.of(page, size));
    }

}
