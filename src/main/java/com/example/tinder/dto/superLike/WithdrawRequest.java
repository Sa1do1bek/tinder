package com.example.tinder.dto.superLike;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {
    private BigDecimal amount;
    private String bankAccount;
    private String bankName;
}