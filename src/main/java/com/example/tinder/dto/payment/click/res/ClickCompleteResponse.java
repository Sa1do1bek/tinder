package com.example.tinder.dto.payment.click.res;

import com.example.tinder.dto.payment.click.req.ClickCompleteRequest;
import com.example.tinder.enums.payment.ClickResponseCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClickCompleteResponse {
    @JsonProperty("click_trans_id")
    private Long clickTransId;

    @JsonProperty("merchant_trans_id")
    private Long merchantTransId;

    @JsonProperty("merchant_confirm_id")
    private Long merchantConfirmId;

    private Integer error;

    @JsonProperty("error_note")
    private String errorNote;

    public static ClickCompleteResponse from(ClickCompleteRequest request, Long merchantConfirmId) {
        return ClickCompleteResponse.builder()
                .clickTransId(request.getClickTransId())
                .merchantTransId(request.getMerchantTransId())
                .merchantConfirmId(merchantConfirmId)
                .error(ClickResponseCode.SUCCESS.getCode())
                .errorNote(ClickResponseCode.SUCCESS.getErrorNote())
                .build();
    }
}
