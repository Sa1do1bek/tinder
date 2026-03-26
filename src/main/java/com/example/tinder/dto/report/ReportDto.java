package com.example.tinder.dto.report;

import com.example.tinder.entity.User;
import com.example.tinder.enums.ReportReason;
import com.example.tinder.enums.ReportStatus;

import java.time.LocalDateTime;

public record ReportDto(
        Long id,
        String comment,
        ReportReason cause,
        LocalDateTime reportedAt,
        ReportStatus status,
        User reportedUser,
        User reporterUser
) {
}
