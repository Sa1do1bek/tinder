package com.example.tinder.controller;

import com.example.tinder.dto.auth.byPhone.LoginByPhoneDto;
import com.example.tinder.dto.auth.byPhone.RegisterByPhoneDto;
import com.example.tinder.dto.auth.byPhone.VerifyByPhoneDto;
import com.example.tinder.dto.TelegramAuthRequest;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.auth.LoginDto;
import com.example.tinder.dto.auth.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.service.AuthService;
import com.google.protobuf.Api;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/loginByEmail")
    public ApiResponse<?> loginByEmail(@Valid @RequestBody LoginDto dto) {
        return this.authService.loginByEmail(dto);
    }

    @PostMapping("/loginByPhone")
    public ApiResponse<?> loginByPhone(@Valid @RequestBody LoginByPhoneDto dto) {
        return this.authService.loginByPhone(dto);
    }

    @PostMapping("/registerByEmail")
    public ApiResponse<?> registerByEmail(@Valid @RequestBody RegisterDto dto) {
        return this.authService.registerByEmail(dto);
    }

    @PostMapping("/registerByPhone")
    public ApiResponse<?> registerByPhone(@Valid @RequestBody RegisterByPhoneDto dto) {
        return this.authService.registerByPhone(dto);
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(@RequestBody VerifyDto dto) {
        return this.authService.verify(dto);
    }

    @PostMapping("/verifyByPhone")
    public ApiResponse<?> verifyByPhone(@RequestBody VerifyByPhoneDto dto) {
        return this.authService.verifyByPhone(dto);
    }

    @PostMapping("/resendCodeToEmail")
    public ApiResponse<?> resendCodeToEmail(@RequestParam("email") String email,
                                            @RequestParam("deviceId") String deviceId,
                                            @RequestParam("id") Long id) {
        return this.authService.resendCodeToEmail(email, deviceId, id);
    }

    @PostMapping("/resendCodeToPhone")
    public ApiResponse<?> resendCodeToPhone(@RequestParam("phone") String phone,
                                            @RequestParam("deviceId") String deviceId,
                                            @RequestParam("id") Long id) {
        return this.authService.resendCodeToPhone(phone, deviceId, id);
    }

    @PostMapping("/checkCodeByEmail")
    public ApiResponse<?> checkCodeByEmail(@RequestParam("email") String email,
                                           @RequestParam("code") String code,
                                           @RequestParam("deviceId") String deviceId) {
        return this.authService.checkCodeByEmail(email, code, deviceId);
    }

    @PostMapping("/checkCodeByPhone")
    public ApiResponse<?> checkCodeByPhone(@RequestParam("phone") String phone,
                                           @RequestParam("code") String code,
                                           @RequestParam("deviceId") String deviceId) {
        return this.authService.checkCodeByPhone(phone, code, deviceId);
    }

    @PostMapping("/checkEmail")
    public ApiResponse<?> checkEmail(@RequestParam("email") String email) {
        return this.authService.checkEmail(email);
    }

    @PostMapping("/checkPhone")
    public ApiResponse<?> checkPhone(@RequestParam("phone") String phone) {
        return this.authService.checkPhone(phone);
    }

    @PostMapping("/forgetPasswordByEmail")
    public ApiResponse<?> forgetPasswordByEmail(@RequestParam("email") String email
            , @RequestParam("deviceId") String deviceName) {
        return this.authService.forgetPasswordByEmail(email, deviceName);
    }


    @PostMapping("/forgetPasswordByPhone")
    public ApiResponse<?> forgetPasswordByPhone(@RequestParam("phone") String phone
            , @RequestParam("deviceId") String deviceName) {
        return this.authService.forgetPasswordByPhone(phone, deviceName);
    }

    @PostMapping("/checkForgetPasswordByEmail")
    public ApiResponse<?> checkForgetPasswordByEmail(@RequestParam("email") String email
            , @RequestParam("code") String code
            , @RequestParam("deviceId") String deviceName
            , @RequestParam("newPassword") String newPassword) {
        return this.authService.checkForgetPasswordByEmail(email, code, deviceName, newPassword);
    }

    @PostMapping("/checkForgetPasswordByPhone")
    public ApiResponse<?> checkForgetPasswordByPhone(@RequestParam("phone") String phone
            , @RequestParam("code") String code
            , @RequestParam("deviceId") String deviceName
            , @RequestParam("newPassword") String newPassword){
        return this.authService.checkForgetPasswordByPhone(phone , code , deviceName , newPassword);
    }
    @PostMapping("/telegram")
    public ApiResponse<?> telegramAuth(@RequestBody TelegramAuthRequest req) {
        return authService.loginOrRegisterByTelegram(req);
    }

}
