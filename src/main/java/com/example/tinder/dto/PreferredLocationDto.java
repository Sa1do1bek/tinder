package com.example.tinder.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreferredLocationDto {
    private Long id;
    private String latitude;
    private String longitude;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreferredLocationCreateDto{
        private String latitude;
        private String longitude;
    }
}
