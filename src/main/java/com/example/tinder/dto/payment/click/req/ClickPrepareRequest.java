package com.example.tinder.dto.payment.click.req;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClickPrepareRequest {
    private Long clickTransId;

    private Integer serviceId;

    private Long merchantTransId;

    private BigDecimal amount;

    private Integer action;

    private Integer error;

    private String errorNote;

    private LocalDateTime signTime;

    private String signString;
}
