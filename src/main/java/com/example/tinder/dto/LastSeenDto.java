package com.example.tinder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class LastSeenDto  {
    private Long userId;
    private Long lastSeen;
}
