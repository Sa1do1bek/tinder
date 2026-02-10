package com.example.tinder.dto.superLike;

import com.example.tinder.enums.TransactionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuperLikePurchaseDto {
    private Long id;
    private Long userId;
    private String username;
    private SuperLikePackageDto packageInfo;
    private Integer quantity;
    private BigDecimal amountPaid;
    private String transactionId;
    private TransactionStatus status;
    private LocalDateTime purchaseDate;
}