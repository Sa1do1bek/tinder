package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceDto {
    private Long id;
    private Integer minAge;
    private Integer maxAge;
    private Double distance;



    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PreferenceUpdateDto {
        private Integer minAge;
        private Integer maxAge;
        private Double distance;
    }



}
