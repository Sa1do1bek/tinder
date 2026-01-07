package com.example.tinder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "superlike_package")
public class SuperLikePackage extends BaseEntity {
    
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal priceUZS;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal PricePerSuperlike;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal platformCommission;
}