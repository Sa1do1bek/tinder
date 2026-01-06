package com.example.tinder.mapper;

import com.example.tinder.dto.superLike.SuperLikePurchaseDto;
import com.example.tinder.entity.SuperLikePurchase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperLikePurchaseMapper {
    private final SuperLikePackageMapper superLikePackageMapper;

    public SuperLikePurchaseDto toDto(SuperLikePurchase purchase) {
        if (purchase == null) return null;
        return SuperLikePurchaseDto.builder()
                .id(purchase.getId())
                .userId(purchase.getUser().getId())
                .username(purchase.getUser().getUsername())
                .packageInfo(superLikePackageMapper.toDto(purchase.getSuperLikePackage()))
                .quantity(purchase.getQuantity())
                .amountPaid(purchase.getAmountPaid())
                .paymentMethod(purchase.getPaymentMethod())
                .transactionId(purchase.getTransactionId())
                .status(purchase.getStatus())
                .purchaseDate(purchase.getCreatedAt())
                .build();
    }

}
