package com.example.tinder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Card extends BaseEntity{
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false, unique = true)
    private String number = UUID.randomUUID().toString();
    private Double balance;
}