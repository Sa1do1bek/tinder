package com.example.tinder.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Long id;
    private Long userId;
    private String number;
    private Double balance;
    
}