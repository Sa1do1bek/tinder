package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.click.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.click.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.click.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.click.res.ClickPrepareResponse;
import com.example.tinder.dto.payment.payme.req.PaymeRequest;
import com.example.tinder.dto.payment.payme.res.PaymeSuccessResponse;

import java.math.BigDecimal;

public interface PaymentService {

    ClickPrepareResponse prepare(ClickPrepareRequest request);

    ClickCompleteResponse complete(ClickCompleteRequest request);

    PaymeSuccessResponse pay(PaymeRequest request);

    ApiResponse<String> getUrlForClick(BigDecimal amount);
}
