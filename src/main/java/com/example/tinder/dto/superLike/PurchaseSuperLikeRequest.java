package com.example.tinder.dto.superLike;

import com.example.tinder.enums.PaymentMethod;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseSuperLikeRequest {
    private Long packageId;
}