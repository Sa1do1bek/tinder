package com.example.tinder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimilarityResponse {

    @JsonProperty("similarity_percent")
    private Double similarityPercent;

}