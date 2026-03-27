package com.example.tinder.dto;


public record UserStatsDTO(
        String period,
        Long UserCount,
        Long userCount,
        Long goldUsers,
        Long approvedUsers
) {}
