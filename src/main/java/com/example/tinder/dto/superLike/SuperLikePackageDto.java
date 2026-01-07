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
    private String savingsPercent; // Chegirma foizi


    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SuperLikePackageCreateDto {
        private Integer quantity;
        private BigDecimal priceUZS;
        private BigDecimal pricePerUnit; // Har birining narxi
        private BigDecimal platformCommission;
    }

}