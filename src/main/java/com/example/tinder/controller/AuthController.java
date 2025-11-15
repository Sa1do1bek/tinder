package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
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
    public ApiResponse loginByEmail(@Valid @RequestBody LoginDto dto) {
        return this.authService.loginByEmail(dto);
    }

    @PostMapping("/registerByEmail")
    public ApiResponse registerByEmail(@Valid @RequestBody RegisterDto dto) {
        return this.authService.registerByEmail(dto);
    }

    @PostMapping("/verify")
    public ApiResponse verify(@RequestBody VerifyDto dto) {
        ApiResponse response = this.authService.verify(dto);
        return response;
    }

    @PostMapping("/resendCodeToEmail")
    public ApiResponse resendCodeToEmail(@RequestParam("email") String email,
                                         @RequestParam("deviceId") String deviceId,
                                         @RequestParam("id") Long id) {
        return this.authService.resendCodeToEmail(email, deviceId, id);
    }

    @PostMapping("/checkCodeByEmail")
    public ApiResponse checkCodeByEmail(@RequestParam("email") String email,
                                        @RequestParam("code") String code,
                                        @RequestParam("deviceId") String deviceId) {
        return this.authService.checkCodeByEmail(email, code, deviceId);
    }
}
