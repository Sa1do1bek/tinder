package com.example.tinder.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserStatsType {
    YEAR("YYYY"),
    MONTH("YYYY-MM"),
    DAY("YYYY-MM-DD");

    private final String format;
}
