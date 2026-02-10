package com.example.tinder.dto.payment.payme.res;

import com.example.tinder.utils.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymeError {
    private Integer code;
    private Message message;
    private String data;

    public PaymeError(com.example.tinder.enums.payment.PaymeError error, String data) {
        this.code = error.getCode();
        this.message = error.getMessage();
        this.data = data;
    }
}
