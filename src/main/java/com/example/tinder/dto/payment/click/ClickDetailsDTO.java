package com.example.tinder.dto.payment.click;


import com.example.tinder.dto.payment.PaymentDetails;
import com.example.tinder.enums.payment.PaymentStatusForClick;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClickDetailsDTO implements PaymentDetails {
    @JsonProperty("click_paydoc_id")
    private Long clickPaydocId;
    @JsonProperty("prepare_time")
    private LocalDateTime prepareTime;
    @JsonProperty("complete_time")
    private LocalDateTime completeTime;
    @JsonProperty("click_cancel_time")
    private LocalDateTime clickCancelTime;
    @JsonProperty("click_status")
    private PaymentStatusForClick clickStatus;
}
