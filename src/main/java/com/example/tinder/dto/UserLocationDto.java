package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLocationDto {
    private String latitude;
    private String longitude;

}
