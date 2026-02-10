package com.example.tinder.dto.payment.click.res;

import com.example.tinder.dto.payment.click.req.ClickPrepareRequest;
import com.example.tinder.enums.payment.ClickResponseCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClickPrepareResponse {
    @JsonProperty("click_trans_id")
    private Long clickTransId;

    @JsonProperty("merchant_trans_id")
    private Long merchantTransId;

    @JsonProperty("merchant_prepare_id")
    private Long merchantPrepareId;

    private Integer error;

    @JsonProperty("error_note")
    private String errorNote;

    public static ClickPrepareResponse from(ClickPrepareRequest request, Long merchantPrepareId) {
        return ClickPrepareResponse.builder()
                .clickTransId(request.getClickTransId())
                .merchantTransId(request.getMerchantTransId())
                .merchantPrepareId(merchantPrepareId)
                .error(ClickResponseCode.SUCCESS.getCode())
                .errorNote(ClickResponseCode.SUCCESS.getErrorNote())
                .build();
    }
}
