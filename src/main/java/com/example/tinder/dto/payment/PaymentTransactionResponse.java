package com.example.tinder.dto.payment;


import com.example.tinder.enums.payment.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentTransactionResponse {

    @JsonProperty("payment_transaction_id")
    private Long id;
    private BigDecimal amount;
    @JsonProperty("user_id")
    private Long userId;
    @JsonProperty("payment_type")
    private PaymentMethod paymentMethod;

    @JsonProperty("merchant_transaction_id")
    private Long merchantTransactionId;

    @JsonProperty("wallet_transaction_id")
    private Long walletTransactionId;

    private PaymentDetails details;
}
