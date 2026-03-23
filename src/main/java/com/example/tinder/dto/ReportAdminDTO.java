package com.example.tinder.dto;

import com.example.tinder.enums.ReportReason;
import com.example.tinder.enums.ReportStatus;

import java.time.LocalDateTime;


public record ReportAdminDTO (
    Long reportId,
    String reporterUsername,
    String reportedUsername,
    ReportReason reportReason,
    ReportStatus reportStatus,
    LocalDateTime createdAt,
    Long reviewedBy,
    String comment,
    LocalDateTime reviewedAt
){}
