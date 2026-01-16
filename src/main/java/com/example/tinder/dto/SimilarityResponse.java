package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityResponse {
    private Double similarity;
}