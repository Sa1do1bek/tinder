package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import org.springframework.stereotype.Component;

@Component
public interface AuthService {

    ApiResponse loginByEmail(LoginDto dto);

    ApiResponse registerByEmail(RegisterDto dto);

    ApiResponse verify(VerifyDto dto);

    ApiResponse resendCodeToEmail(String email,String deviceId,Long id);

    ApiResponse checkCodeByEmail(String email, String code,String deviceId);
}
