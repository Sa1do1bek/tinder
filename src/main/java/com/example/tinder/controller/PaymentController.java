package com.example.tinder.controller;

import com.example.tinder.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/click/prepare")
    public Map<String, Object> prepare(@RequestParam Map<String, String> params) {
        return paymentService.prepare(params);
    }

    @PostMapping("/click/complete")
    public Map<String, Object> complete(@RequestParam Map<String, String> params) {
        return paymentService.complete(params);
    }

    @PostMapping("/payme")
    public Map<String, Object> handle(@RequestBody Map<String, Object> body,
                                      @RequestHeader("Authorization") String auth) {
        return paymentService.handle(body, auth);
    }
}
