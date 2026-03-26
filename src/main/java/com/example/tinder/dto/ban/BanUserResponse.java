package com.example.tinder.dto.ban;

import com.example.tinder.enums.UserStatus;

import java.time.LocalDateTime;

public record BanUserResponse(
        Long userId,
        String username,
        UserStatus userStatus,
        LocalDateTime pannedAt,
        LocalDateTime panUtil,
        boolean permanent,
        String panReason
) {
}
