package com.example.tinder.dto.superLike;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuperLikePackageDto {
    private Long id;
    private Integer quantity;
    private BigDecimal priceUZS;
    private BigDecimal pricePerUnit; // Har birining narxi
    private BigDecimal platformCommission;
    private Boolean isActive;
    private String savingsPercent; // Chegirma foizi
}