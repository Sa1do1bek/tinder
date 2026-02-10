package com.example.tinder.exception;

import com.example.tinder.enums.payment.PaymeError;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymeException extends RuntimeException {
    private PaymeError error;
    private String data;

    public PaymeException(PaymeError error) {
        this.error = error;
    }
}
