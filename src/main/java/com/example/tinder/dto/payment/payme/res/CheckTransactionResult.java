package com.example.tinder.dto.payment.payme.res;

import com.example.tinder.entity.PaymentTransaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckTransactionResult {
    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("perform_time")
    private Long performTime;

    @JsonProperty("cancel_time")
    private Long cancelTime;

    private String transaction;

    private Integer state;

    private Integer reason;

    public static CheckTransactionResult from(PaymentTransaction transaction) {
        return CheckTransactionResult.builder()
                .createTime(transaction.getCreateTime())
                .performTime(transaction.getPerformTime())
                .cancelTime(transaction.getCancelTime())
                .transaction(transaction.getProviderTransactionId())
                .state(transaction.getState().getState())
                .reason(transaction.getReason())
                .build();
    }
}
