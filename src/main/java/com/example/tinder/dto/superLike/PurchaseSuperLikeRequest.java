package com.example.tinder.dto.superLike;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseSuperLikeRequest {
    private Long packageId;
}