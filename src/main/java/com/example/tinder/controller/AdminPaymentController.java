package com.example.tinder.controller;


import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.payment.PaymentMethod;
import com.example.tinder.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/payment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/all-payment")
    public ApiResponse<Page<?>> allPayment(@RequestParam(required = false) LocalDate start,
                                           @RequestParam(required = false) LocalDate end,
                                           @RequestParam(required = false) PaymentMethod paymentMethod,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size
    ){
        return paymentService.getAllPayment(start,end,paymentMethod,page,size);
    }
}
