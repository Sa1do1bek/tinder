package com.example.tinder.entity;

import com.example.tinder.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code; // GOLD_1_MONTH

    @Enumerated(EnumType.STRING)
    private SubscriptionType type;

    private int months;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;
}