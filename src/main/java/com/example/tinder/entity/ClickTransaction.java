package com.example.tinder.entity;

import com.example.tinder.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickTransaction extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String clickTransId;

    private Long userId;

    @Column(precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
