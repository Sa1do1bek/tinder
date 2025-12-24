package com.example.tinder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryResponseDTO {
    private Long id;
    private String fullname;
    private int age;
    private String bio;
    private String gender;
    
    private Double distanceKm;
    private Double height;
    
    private List<String> profilePictures; // Faqat cloudPath'lar
    private List<UserInterestDto> interests;
}