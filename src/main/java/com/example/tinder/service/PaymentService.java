package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.res.ClickPrepareResponse;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentService {

    ClickPrepareResponse prepare(ClickPrepareRequest request);

    ClickCompleteResponse complete(ClickCompleteRequest request);

    Map<String, Object> handle(Map<String, Object> body, String authHeader);

    ApiResponse<String> getUrlForClick(BigDecimal amount);
}
