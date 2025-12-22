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
    private int age; // birthDate dan hisoblanadi
    private String bio;
    private String gender;
    
    // Essential qismi
    private Double distanceKm; // Masofa (hisoblab chiqariladi)
    private Double height; // Agar User entityda bo'lsa
    
    // Ro'yxatlar
    private List<String> profilePictures; // Faqat cloudPath'lar
    private List<UserInterestDto> interests; // Qiziqishlar nomi va kodi
}