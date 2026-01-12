package com.example.tinder.controller;

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

    @PostMapping("/registerByEmail")
    public ApiResponse<?> registerByEmail(@Valid @RequestBody RegisterDto dto) {
        return this.authService.registerByEmail(dto);
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(@RequestBody VerifyDto dto) {
        return (ApiResponse<?>) this.authService.verify(dto);
    }

    @PostMapping("/resendCodeToEmail")
    public ApiResponse<?> resendCodeToEmail(@RequestParam("email") String email,
                                            @RequestParam("deviceId") String deviceId,
                                            @RequestParam("id") Long id) {
        return this.authService.resendCodeToEmail(email, deviceId, id);
    }

    @PostMapping("/checkCodeByEmail")
    public ApiResponse<?> checkCodeByEmail(@RequestParam("email") String email,
                                           @RequestParam("code") String code,
                                           @RequestParam("deviceId") String deviceId) {
        return this.authService.checkCodeByEmail(email, code, deviceId);
    }

    @PostMapping("/checkEmail")
    public ApiResponse<?> checkEmail(@RequestParam("email") String email) {
        return this.authService.checkEmail(email);
    }

    @PostMapping("/forgetPasswordByEmail")
    public ApiResponse<?> forgetPasswordByEmail(@RequestParam("email") String email, @RequestParam("deviceId") String deviceName) {
        return authService.forgetPasswordByEmail(email, deviceName);
    }

    @PostMapping("/checkForgetPasswordByEmail")
    public ApiResponse<?> checkForgetPasswordByEmail(@RequestParam("email") String email, @RequestParam("code") String code, @RequestParam("deviceId") String deviceName , @RequestParam("newPassword") String newPassword) {
        return authService.checkForgetPasswordByEmail(email, code, deviceName , newPassword);
    }

    @PostMapping("/telegram")
    public ApiResponse<?> telegramAuth(@RequestBody TelegramAuthRequest req) {
        return authService.loginOrRegisterByTelegram(req);
    }

}
