package com.example.tinder.utils;


import com.example.tinder.dto.payment.payme.res.GetTransactionDto;
import com.example.tinder.entity.PaymeTransaction;
import com.example.tinder.enums.payment.PaymeError;
import com.example.tinder.exception.PaymeException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class PaymeUtil {
    private static final String username = "Paycom";
    private static final String password = "";
    private static final String merchantId = "";

    /**
     * Payme bizga summani tiyn ko'rinishida yuboradi lekin biz so'mda saqlaymiz
     * shuning uchun biz summani 100 ga bo'lib qaytarib beramiz
     */
    public static Long normalizeAmount(Long amount) {
        return (long) Math.floor((double) amount / 100);
    }

    public static List<GetTransactionDto> mapToGetTransactionDto(List<PaymeTransaction> transactionList) {
        return transactionList.stream().map(GetTransactionDto::from).toList();
    }

    /**
     * bu methodda biz requestni haqiqatdan ham Payme tomonidan yuborilganligini
     * tekshiramiz
     */
    public static void authorizeToken(String authHeader) {
        final String basic = "Basic ";

        if (authHeader == null || !authHeader.startsWith(basic)) {
            throw new PaymeException(PaymeError.INVALID_AUTHORIZATION);
        }

        String tokenBase64 = authHeader.substring(basic.length());

        String token = new String(Base64.getDecoder().decode(tokenBase64), StandardCharsets.UTF_8);

        String[] auth = token.split(":");
        if (!username.equals(auth[0]) || !password.equals(auth[1])) {
            throw new PaymeException(PaymeError.INVALID_AUTHORIZATION);
        }
    }

    /**
     * bu method bizga to'lov uchun link yaratadi
     * bu link orqali foydalanuvchi to'lovni amalga oshiradi
     */
    public static String generatePaymentUrl(Long userId, BigDecimal amount) {

        String str = String.format("m=%s;ac.order_id=%s;a=%s", merchantId, userId, amount);

        String encoded = Base64.getEncoder().encodeToString(str.getBytes());

        return "https://checkout.paycom.uz/" + encoded;
    }
}
