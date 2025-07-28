package com.example.parking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "oil_change_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OilChangeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate lastChangeDate;

    @Column(nullable = false)
    private Integer lastChangeMileage;

    @Column(nullable = false)
    private String oilBrand;

    @Column(nullable = false)
    private Integer recommendedKm;

    @Column(nullable = false)
    private Integer dailyDistance;

    @Column(nullable = false)
    private LocalDate nextChangeDate;

    @Column(nullable = false)
    private Integer nextChangeMileage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "telegram_user_id", nullable = false)
    private UserBot user;
}