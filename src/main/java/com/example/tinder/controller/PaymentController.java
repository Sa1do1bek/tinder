package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.click.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.click.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.click.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.click.res.ClickPrepareResponse;
import com.example.tinder.dto.payment.payme.req.PaymeRequest;
import com.example.tinder.dto.payment.payme.res.PaymeSuccessResponse;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import com.example.tinder.utils.PaymeUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping(value = "/prepare")
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
        System.out.println("****************************************************");
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

    @PostMapping(value = "/complete")
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

    @PostMapping("/payme")
    public PaymeSuccessResponse paymeTransaction(@RequestBody PaymeRequest request, HttpServletRequest httpServletRequest){
        PaymeUtil.authorizeToken(httpServletRequest.getHeader("Authorization"));

        return paymentService.pay(request);
    }

}
