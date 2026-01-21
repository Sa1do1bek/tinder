package com.example.tinder.service;

import java.util.Map;

public interface PaymentService {

    Map<String, Object> prepare(Map<String, String> params);

    Map<String, Object> complete(Map<String, String> params);

    Map<String, Object> handle(Map<String, Object> body, String authHeader);
}
