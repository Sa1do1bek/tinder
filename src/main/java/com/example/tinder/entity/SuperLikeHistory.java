package com.example.tinder.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "superlike_history")
public class SuperLikeHistory extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal rewardAmount; // 5000 som
    
    @Column(columnDefinition = "boolean default false", precision = 15, scale = 2)
    private Boolean isRewardPaid = false;
}