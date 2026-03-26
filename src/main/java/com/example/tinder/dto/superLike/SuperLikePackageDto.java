package com.example.tinder.dto.superLike;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
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
    private BigDecimal oldPriceUZs; //eski narxi
    private BigDecimal pricePerUnit; // Har birining narxi
    private BigDecimal platformCommission;
    private Integer discountPercent; // string savingsPercent ni o'rniga integer discountPercent ni qo'ydim chunki hisoblashda qiylashadi front % ni o'zi dizayni qilib chiqadi.


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
        @Builder.Default
        @JsonSetter(nulls = Nulls.SET)//default holatda frontdan null kiritish uchun agar ota package bo'lmasa
        private Long parentPackageId=null;
    }

}