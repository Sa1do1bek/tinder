package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class LocationCoordinate {
        private Double latitude;
        private Double longitude;
    }