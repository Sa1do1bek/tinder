package com.example.tinder.dto;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationDto {
    private Long id;
    private Long userId;
    private String matchImageURL;
    private String faceImageURL;
    private Double similarityScore;


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class VerificationUpdateDto {
        private String matchImageURL;
        private String faceImageURL;
        private Double similarityScore;
    }
}
