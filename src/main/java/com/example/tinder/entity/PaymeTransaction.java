package com.example.tinder.entity;

import com.example.tinder.dto.payment.payme.req.PaymeParam;
import com.example.tinder.enums.payment.PaymeTransactionState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity(name = "payme_transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymeTransaction {
    @Id
    private String id;

    private Long userId;

    private BigDecimal amount;

    private long createTime;

    private long performTime;

    private long cancelTime;

    @Enumerated(EnumType.STRING)
    private PaymeTransactionState state;

    private Integer reason;

    private Long walletTransactionId;

    public static PaymeTransaction from(PaymeParam params) {
        return PaymeTransaction.builder()
                .id(params.getId())
                .userId(params.getAccount().getUserId())
                .amount(params.getAmount())
                .createTime(System.currentTimeMillis())
                .state(PaymeTransactionState.PENDING)
                .build();
    }
}
