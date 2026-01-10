package com.example.tinder.dto;

import com.example.tinder.enums.SubscriptionType;
import lombok.*;

import java.util.List;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryResponseDTO {
    private Long id;
    private String fullname;
    private int age;
    private String bio;
    private String gender;
    private SubscriptionType subscriptionType;
    
    private Double distanceKm;
    private Double height;
    
    private List<String> profilePictures; // Faqat cloudPath'lar
    private List<UserInterestDto> interests;

    private String latitude;
    private String longitude;
}