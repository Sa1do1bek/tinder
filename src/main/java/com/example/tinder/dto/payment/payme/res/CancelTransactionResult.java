package com.example.tinder.dto.payment.payme.res;

import com.example.tinder.entity.PaymentTransaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelTransactionResult {
    private String transaction;

    @JsonProperty("cancel_time")
    private Long cancelTime;

    private int state;

    public static CancelTransactionResult from(PaymentTransaction paymeTransaction) {
        return CancelTransactionResult.builder()
                .transaction(paymeTransaction.getProviderTransactionId())
                .cancelTime(paymeTransaction.getCancelTime())
                .state(paymeTransaction.getState().getState())
                .build();
    }
}
