package com.example.tinder.controller;

import com.example.tinder.annotation.LogUserAction;
import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.UserAction;
import com.example.tinder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/loginByEmail")
    @LogUserAction(action = UserAction.LOGIN, logRequestBody = true)
    public ApiResponse loginByEmail(@Valid @RequestBody LoginDto dto) {
        return this.authService.loginByEmail(dto);
    }

    @PostMapping("/registerByEmail")
    @LogUserAction(action = UserAction.REGISTER, logRequestBody = true)
    public ApiResponse registerByEmail(@Valid @RequestBody RegisterDto dto) {
        return this.authService.registerByEmail(dto);
    }

    @PostMapping("/verify")
    @LogUserAction(action = UserAction.VERIFY_EMAIL, logRequestBody = true)
    public ApiResponse verify(@RequestBody VerifyDto dto) {
        return this.authService.verify(dto);
    }

    @PostMapping("/resendCodeToEmail")
    @LogUserAction(action = UserAction.VERIFY_EMAIL)
    public ApiResponse resendCodeToEmail(@RequestParam("email") String email,
                                         @RequestParam("deviceId") String deviceId,
                                         @RequestParam("id") Long id) {
        return this.authService.resendCodeToEmail(email, deviceId, id);
    }

    @PostMapping("/checkCodeByEmail")
    @LogUserAction(action = UserAction.VERIFY_EMAIL)
    public ApiResponse checkCodeByEmail(@RequestParam("email") String email,
                                        @RequestParam("code") String code,
                                        @RequestParam("deviceId") String deviceId,
                                        @RequestParam("fcmToken") String fcmToken) {
        return this.authService.checkCodeByEmail(email, code, deviceId, fcmToken);
    }
}
