package com.example.tinder.dto;

import lombok.Data;

@Data
public class UserLocationDto {
    private Long userId;
    private String latitude;
    private String longitude;

}
