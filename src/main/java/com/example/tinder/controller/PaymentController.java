package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.click.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.click.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.click.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.click.res.ClickPrepareResponse;
import com.example.tinder.dto.payment.payme.req.PaymeRequest;
import com.example.tinder.dto.payment.payme.res.PaymeSuccessResponse;
import com.example.tinder.entity.Log;
import com.example.tinder.enums.payment.ClickResponseCode;
import com.example.tinder.exception.ClickException;
import com.example.tinder.repository.LogRepository;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import com.example.tinder.utils.PaymeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final LogRepository logRepository;

    @GetMapping("/getUrlForClick")
    public ApiResponse<String> getUrlForClick(@RequestParam BigDecimal amount) {
        return paymentService.getUrlForClick(amount);
    }

    @PostMapping(value = {"/prepare", "/click/prepare"}, consumes = "application/x-www-form-urlencoded")
    public ClickPrepareResponse prepare(
            @RequestParam("click_trans_id") Long clickTransId,
            @RequestParam("service_id") Integer serviceId,
            @RequestParam("merchant_trans_id") Long merchantTransId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("action") Integer action,
            @RequestParam("error") Integer error,
            @RequestParam(value = "error_note", required = false, defaultValue = "") String errorNote,
            @RequestParam("sign_time") String signTime,
            @RequestParam("sign_string") String signString
    ) {
        try {
            logRepository.save(
                    Log.builder()
                            .text("Request received")
                            .date(LocalDateTime.now())
                            .build()
            );
        } catch (Exception ex) {
            log.error("Unable to save request received log", ex);
        }


        try {
            ClickPrepareRequest request = ClickPrepareRequest.builder()
                    .clickTransId(clickTransId)
                    .serviceId(serviceId)
                    .merchantTransId(merchantTransId)
                    .amount(amount)
                    .action(action)
                    .error(error)
                    .errorNote(errorNote)
                    .signTime(signTime)
                    .signString(signString)
                    .build();
            logRepository.save(
                    Log.builder()
                            .text("Request body: " + request.toString())
                            .date(LocalDateTime.now())
                            .build()
            );

            ClickUtil.authorizeSignString(request);
            logRepository.save(
                    Log.builder()
                            .text("Request authorized successfull ")
                            .date(LocalDateTime.now())
                            .build()
            );
            return paymentService.prepare(request);
        } catch (ClickException e) {
            logRepository.save(
                    Log.builder()
                            .text("Exception occurred in Payment Controller prepare: " + e.getMessage())
                            .date(LocalDateTime.now())
                            .build()
            );
            return ClickPrepareResponse.builder()
                    .clickTransId(clickTransId)
                    .merchantTransId(merchantTransId)
                    .error(e.getCode().getCode())
                    .errorNote(e.getCode().getErrorNote())
                    .build();
        } catch (Exception e) {
            logRepository.save(
                    Log.builder()
                            .text("Click prepare parsing/internal error " + e.getMessage())
                            .date(LocalDateTime.now())
                            .build()
            );
            log.error("Click prepare parsing/internal error", e);
            return ClickPrepareResponse.builder()
                    .clickTransId(clickTransId)
                    .merchantTransId(merchantTransId)
                    .error(ClickResponseCode.BAD_REQUEST.getCode())
                    .errorNote(ClickResponseCode.BAD_REQUEST.getErrorNote())
                    .build();
        }
    }

    @PostMapping(value = {"/complete", "/click/complete"}, consumes = "application/x-www-form-urlencoded")
    public ClickCompleteResponse complete(
            @RequestParam("click_trans_id") Long clickTransId,
            @RequestParam("service_id") Integer serviceId,
            @RequestParam("merchant_trans_id") Long merchantTransId,
            @RequestParam(value = "merchant_prepare_id") Long merchantPrepareId,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("action") Integer action,
            @RequestParam("error") Integer error,
            @RequestParam(value = "error_note", required = false, defaultValue = "") String errorNote,
            @RequestParam("sign_time") String signTime,
            @RequestParam("sign_string") String signString
    ) {
        try {
            logRepository.save(
                    Log.builder()
                            .text("Complete endpoint ")
                            .date(LocalDateTime.now())
                            .build()
            );
        } catch (Exception ex) {
            log.error("Unable to save complete endpoint log", ex);
        }

        try {
            ClickCompleteRequest request = ClickCompleteRequest.builder()
                    .clickTransId(clickTransId)
                    .serviceId(serviceId)
                    .merchantTransId(merchantTransId)
                    .merchantPrepareId(merchantPrepareId)
                    .amount(amount)
                    .action(action)
                    .error(error)
                    .errorNote(errorNote)
                    .signTime(signTime)
                    .signString(signString)
                    .build();

            ClickUtil.authorizeSignString(request);
            logRepository.save(
                    Log.builder()
                            .text("Complete method Calling in Payment Controller")
                            .date(LocalDateTime.now())
                            .build()
            );
            return paymentService.complete(request);
        } catch (ClickException e) {
            logRepository.save(
                    Log.builder()
                            .text("Exception occurred in Payment Controller complete: " + e.getMessage())
                            .date(LocalDateTime.now())
                            .build()
            );
            return ClickCompleteResponse.builder()
                    .clickTransId(clickTransId)
                    .merchantTransId(merchantTransId)
                    .error(e.getCode().getCode())
                    .errorNote(e.getCode().getErrorNote())
                    .build();
        } catch (Exception e) {
            logRepository.save(
                    Log.builder()
                            .text("Click complete parsing/internal error " + e.getMessage())
                            .date(LocalDateTime.now())
                            .build()
            );
            log.error("Click complete parsing/internal error", e);
            return ClickCompleteResponse.builder()
                    .clickTransId(clickTransId)
                    .merchantTransId(merchantTransId)
                    .error(ClickResponseCode.BAD_REQUEST.getCode())
                    .errorNote(ClickResponseCode.BAD_REQUEST.getErrorNote())
                    .build();
        }
    }

    @PostMapping("/payme")
    public PaymeSuccessResponse paymeTransaction(@RequestBody PaymeRequest request, HttpServletRequest httpServletRequest){
        PaymeUtil.authorizeToken(httpServletRequest.getHeader("Authorization"));

        return paymentService.pay(request);
    }

}
