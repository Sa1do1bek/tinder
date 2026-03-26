package com.example.tinder.dto.ban;

import java.time.LocalDateTime;

public record BanUserRequest(
        boolean permanent,
        LocalDateTime banUtil,
        String banReason
) {
}
