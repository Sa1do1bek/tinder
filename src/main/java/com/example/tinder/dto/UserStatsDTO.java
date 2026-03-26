package com.example.tinder.dto;


public record UserStatsDTO(
        String period,
        Long UserCount,
        Long goldUsers,
        Long approvedUsers
) {}
