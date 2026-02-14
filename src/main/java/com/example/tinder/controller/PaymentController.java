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
import java.util.Map;

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

    @PostMapping(value = {"/prepare", "/click/prepare"})
    public ClickPrepareResponse prepare(
            @RequestParam Map<String, String> params
    ) {
        logRepository.save(
                Log.builder()
                        .text("Prepare endpoint ")
                        .date(LocalDateTime.now())
                        .build()
        );

        Long clickTransId = parseLong(params.get("click_trans_id"));
        Long merchantTransId = parseLong(params.get("merchant_trans_id"));

        try {
            ClickPrepareRequest request = ClickPrepareRequest.builder()
                    .clickTransId(clickTransId)
                    .serviceId(parseInteger(params.get("service_id")))
                    .merchantTransId(merchantTransId)
                    .amount(parseBigDecimal(params.get("amount")))
                    .action(parseInteger(params.get("action")))
                    .error(parseInteger(params.get("error")))
                    .errorNote(params.get("error_note"))
                    .signTime(params.get("sign_time"))
                    .signString(params.get("sign_string"))
                    .build();

            ClickUtil.authorizeSignString(request);
            logRepository.save(
                    Log.builder()
                            .text("Prepare Method Calling ")
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

    @PostMapping(value = {"/complete", "/click/complete"})
    public ClickCompleteResponse complete(
            @RequestParam Map<String, String> params
    ) {
        logRepository.save(
                Log.builder()
                        .text("Complete endpoint ")
                        .date(LocalDateTime.now())
                        .build()
        );
        Long clickTransId = parseLong(params.get("click_trans_id"));
        Long merchantTransId = parseLong(params.get("merchant_trans_id"));

        try {
            ClickCompleteRequest request = ClickCompleteRequest.builder()
                    .clickTransId(clickTransId)
                    .serviceId(parseInteger(params.get("service_id")))
                    .merchantTransId(merchantTransId)
                    .merchantPrepareId(parseLong(params.get("merchant_prepare_id")))
                    .amount(parseBigDecimal(params.get("amount")))
                    .action(parseInteger(params.get("action")))
                    .error(parseInteger(params.get("error")))
                    .errorNote(params.get("error_note"))
                    .signTime(params.get("sign_time"))
                    .signString(params.get("sign_string"))
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

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Long.parseLong(value.trim());
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value.trim());
    }

}
