package com.example.tinder.mapper.payment;

import com.example.tinder.dto.payment.PaymentTransactionProjection;
import com.example.tinder.dto.payment.PaymentTransactionResponse;
import com.example.tinder.dto.payment.click.ClickDetailsDTO;
import com.example.tinder.dto.payment.payme.PaymeDetailsDTO;
import com.example.tinder.enums.payment.PaymentMethod;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionMapper {

    public PaymentTransactionResponse toResponse(PaymentTransactionProjection t) {

        PaymentTransactionResponse res = new PaymentTransactionResponse();

        res.setId(t.getId());
        res.setAmount(t.getAmount());
        res.setUserId(t.getUserId());
        res.setPaymentMethod(t.getPaymentMethod());
        res.setMerchantTransactionId(t.getMerchantTransactionId());
        res.setWalletTransactionId(t.getWalletTransactionId());

        if (t.getPaymentMethod() == PaymentMethod.PAYME) {

            PaymeDetailsDTO dto = new PaymeDetailsDTO();
            dto.setCreateTime(t.getCreateTime());
            dto.setPerformTime(t.getPerformTime());
            dto.setCancelTime(t.getCancelTime());
            dto.setState(t.getState());
            dto.setReason(t.getReason());

            res.setDetails(dto);

        } else if (t.getPaymentMethod() == PaymentMethod.CLICK) {

            ClickDetailsDTO dto = new ClickDetailsDTO();
            dto.setClickPaydocId(t.getClickPaydocId());
            dto.setPrepareTime(t.getPrepareTime());
            dto.setCompleteTime(t.getCompleteTime());
            dto.setClickCancelTime(t.getClickCancelTime());
            dto.setClickStatus(t.getClickStatus());

            res.setDetails(dto);
        }

        return res;
    }
}
