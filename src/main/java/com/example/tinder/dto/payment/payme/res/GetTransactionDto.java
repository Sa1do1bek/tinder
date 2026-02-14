package com.example.tinder.dto.payment.payme.res;

import com.example.tinder.dto.payment.payme.req.PaymeAccount;
import com.example.tinder.entity.PaymentTransaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetTransactionDto {
    private String id;

    private Long time;

    private BigDecimal amount;

    @JsonProperty("create_time")
    private Long createTime;

    @JsonProperty("perform_time")
    private Long performTime;

    @JsonProperty("cancel_time")
    private Long cancelTime;

    private PaymeAccount account;

    private String transaction;

    private Integer state;

    private Integer reason;

    public static GetTransactionDto from(PaymentTransaction paymeTransaction) {
        return GetTransactionDto.builder()
                .id(paymeTransaction.getProviderTransactionId())
                .time(paymeTransaction.getCreateTime())
                .amount(paymeTransaction.getAmount())
                .createTime(paymeTransaction.getCreateTime())
                .performTime(paymeTransaction.getPerformTime())
                .cancelTime(paymeTransaction.getCancelTime())
                .account(new PaymeAccount(paymeTransaction.getUserId()))
                .transaction(paymeTransaction.getProviderTransactionId())
                .state(paymeTransaction.getState().getState())
                .reason(paymeTransaction.getReason())
                .build();
    }
}
