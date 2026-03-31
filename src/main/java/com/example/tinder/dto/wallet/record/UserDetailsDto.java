package com.example.tinder.dto.wallet.record;

public record UserDetailsDto(
        Long id,
        String username,
        String profilePic,
        String email,
        String phoneNumber
        ) {}
