package com.example.tinder.dto.payment.payme.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymeParam {
    private String id;
    private PaymeAccount account;
    private BigDecimal amount;
    private Long time;
    private Integer reason;
    private Long from;
    private Long to;
}
