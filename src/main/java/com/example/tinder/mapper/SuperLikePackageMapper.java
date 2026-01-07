package com.example.tinder.mapper;

import com.example.tinder.dto.superLike.SuperLikePackageDto;
import com.example.tinder.entity.SuperLikePackage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class SuperLikePackageMapper {

    public SuperLikePackageDto toDto(SuperLikePackage pkg) {
        if (pkg == null) return null;
        BigDecimal pricePerUnit = pkg.getPriceUZS().divide(
                new BigDecimal(pkg.getQuantity()), 2, RoundingMode.HALF_UP);

        // Chegirmani hisoblash (3 talik paket base hisoblansa)
        BigDecimal basePricePerUnit = new BigDecimal("15640"); // 46920 / 3
        BigDecimal savings = BigDecimal.ZERO;
        if (pkg.getQuantity() > 3) {
            savings = basePricePerUnit.subtract(pricePerUnit)
                    .divide(basePricePerUnit, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return SuperLikePackageDto.builder()
                .id(pkg.getId())
                .quantity(pkg.getQuantity())
                .priceUZS(pkg.getPriceUZS())
                .pricePerUnit(pricePerUnit)
                .platformCommission(pkg.getPlatformCommission())
                .savingsPercent(savings.intValue() > 0 ? savings.intValue() + "%" : null)
                .build();
    }


    public SuperLikePackage fromCreateDto(SuperLikePackageDto.SuperLikePackageCreateDto pkg) {
        if (pkg == null) return null;

        SuperLikePackage entity = new SuperLikePackage();
        entity.setQuantity(pkg.getQuantity());
        entity.setPriceUZS(pkg.getPriceUZS());
        entity.setPricePerSuperlike(pkg.getPricePerUnit());
        entity.setPlatformCommission(pkg.getPlatformCommission());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVisibility(true);

        return entity;
    }

}
