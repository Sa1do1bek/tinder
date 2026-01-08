package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Card extends BaseEntity{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true)
    private String number = UUID.randomUUID().toString();

    @Column(precision = 15, scale = 2)
    private BigDecimal balance;

    private Integer superLikeCount = 0;
    private Integer totalSuperLikesEarned = 0;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalEarnedUZS;
}