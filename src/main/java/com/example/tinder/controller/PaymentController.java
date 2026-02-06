package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.res.ClickPrepareResponse;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/getUrlForClick")
    public ApiResponse<String> getUrlForClick(@RequestParam BigDecimal amount) {
        return paymentService.getUrlForClick(amount);
    }

    @PostMapping(value = "/prepare", consumes = "application/x-www-form-urlencoded")
    public ClickPrepareResponse prepare(
            @RequestParam("click_trans_id") Long clickTransId,
            @RequestParam("service_id") Integer serviceId,
            @RequestParam("merchant_trans_id") Long merchantTransId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("action") Integer action,
            @RequestParam("error") Integer error,
            @RequestParam("error_note") String errorNote,
            @RequestParam("sign_time") String signTime,
            @RequestParam("sign_string") String signString
    ) {
        log.info("Prepare request");

        ClickPrepareRequest build = ClickPrepareRequest.builder()
                .clickTransId(clickTransId)
                .serviceId(serviceId)
                .merchantTransId(merchantTransId)
                .amount(amount)
                .action(action)
                .error(error)
                .errorNote(errorNote)
                .signTime(ClickUtil.parseSignTime(signTime))
                .signString(signString)
                .build();

        ClickUtil.authorizeSignString(build);

        return paymentService.prepare(build);
    }

    @PostMapping(value = "/complete", consumes = "application/x-www-form-urlencoded")
    public ClickCompleteResponse complete(
            @RequestParam("click_trans_id") Long clickTransId,
            @RequestParam("service_id") Integer serviceId,
            @RequestParam("merchant_trans_id") Long merchantTransId,
            @RequestParam(value = "merchant_prepare_id") Long merchantPrepareId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("action") Integer action,
            @RequestParam("error") Integer error,
            @RequestParam("error_note") String errorNote,
            @RequestParam("sign_time") String signTime,
            @RequestParam("sign_string") String signString
    ) {
        log.info("Complete request");

        ClickCompleteRequest build = ClickCompleteRequest.builder()
                .clickTransId(clickTransId)
                .serviceId(serviceId)
                .merchantTransId(merchantTransId)
                .merchantPrepareId(merchantPrepareId)
                .amount(amount)
                .action(action)
                .error(error)
                .errorNote(errorNote)
                .signTime(ClickUtil.parseSignTime(signTime))
                .signString(signString)
                .build();

        ClickUtil.authorizeSignString(build);

        return paymentService.complete(build);
    }
}
