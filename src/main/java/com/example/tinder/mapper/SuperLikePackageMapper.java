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

        Integer discount = null;
        BigDecimal oldPrice=null;

        //  parent bo‘lsa discount hisoblaymiz
        if (pkg.getParentPackage() != null) {
            oldPrice = pkg.getParentPackage().getPriceUZS();
            if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
                discount = oldPrice.subtract(pkg.getPriceUZS())
                        .multiply(BigDecimal.valueOf(100))
                        .divide(oldPrice, 0, RoundingMode.HALF_UP)
                        .intValue();
            }
        }

        return SuperLikePackageDto.builder()
                .id(pkg.getId())
                .quantity(pkg.getQuantity())
                .priceUZS(pkg.getPriceUZS())
                .oldPriceUZs(oldPrice)
                .pricePerUnit(pricePerUnit)
                .platformCommission(pkg.getPlatformCommission())
                .discountPercent(discount)
                .build();
    }


    public SuperLikePackage fromCreateDto(SuperLikePackageDto.SuperLikePackageCreateDto pkg) {
        if (pkg == null) return null;

        SuperLikePackage entity = new SuperLikePackage();
        entity.setQuantity(pkg.getQuantity());
        entity.setPriceUZS(pkg.getPriceUZS());

        // per unit ni backend hisoblaydi
        BigDecimal pricePerUnit = pkg.getPriceUZS().divide(
                new BigDecimal(pkg.getQuantity()), 2, RoundingMode.HALF_UP);
        entity.setPricePerSuperlike(pricePerUnit);
        entity.setPlatformCommission(pkg.getPlatformCommission());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVisibility(true);

        return entity;
    }

}
