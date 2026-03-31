package com.example.tinder.dto.payment.payme;

import com.example.tinder.dto.payment.PaymentDetails;
import com.example.tinder.enums.payment.PaymeTransactionState;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymeDetailsDTO implements PaymentDetails {
    @JsonProperty("create_time")
    private Long createTime;
    @JsonProperty("perform_time")
    private Long performTime;
    @JsonProperty("cancel_time")
    private Long cancelTime;
    @JsonProperty("payme_transaction_state")
    private PaymeTransactionState state;
    private Integer reason;

}
