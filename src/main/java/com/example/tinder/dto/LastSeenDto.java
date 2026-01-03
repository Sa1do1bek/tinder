package com.example.tinder.dto;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LastSeenDto  {
    private Long userId;
    private Long lastSeen;
}
