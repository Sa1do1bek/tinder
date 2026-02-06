package com.example.tinder.dto.payment.res;

import com.example.tinder.dto.payment.req.ClickCompleteRequest;
import com.example.tinder.enums.ClickResponseCode;
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
    private Integer merchantConfirmId;

    private Integer error;

    @JsonProperty("error_note")
    private String errorNote;

    public static ClickCompleteResponse from(ClickCompleteRequest request) {
        return ClickCompleteResponse.builder()
                .clickTransId(request.getClickTransId())
                .merchantTransId(request.getMerchantTransId())
                .error(ClickResponseCode.SUCCESS.getCode())
                .errorNote(ClickResponseCode.SUCCESS.getErrorNote())
                .build();
    }
}
