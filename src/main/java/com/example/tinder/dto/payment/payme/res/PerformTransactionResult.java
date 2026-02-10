package com.example.tinder.dto.payment.payme.res;

import com.example.tinder.entity.PaymeTransaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformTransactionResult {
    private String transaction;

    @JsonProperty("perform_time")
    private Long performTime;

    private int state;

    public static PerformTransactionResult from(PaymeTransaction transaction) {
        return PerformTransactionResult.builder()
                .transaction(transaction.getId())
                .performTime(transaction.getPerformTime())
                .state(transaction.getState().getState())
                .build();
    }
}
