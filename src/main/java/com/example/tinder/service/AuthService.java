package com.example.tinder.service;

import com.example.tinder.dto.auth.byPhone.LoginByPhoneDto;
import com.example.tinder.dto.auth.byPhone.RegisterByPhoneDto;
import com.example.tinder.dto.auth.byPhone.VerifyByPhoneDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.auth.LoginDto;
import com.example.tinder.dto.auth.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public interface AuthService {

    ApiResponse<?> loginByEmail(LoginDto dto);

    ApiResponse<?> loginByPhone(LoginByPhoneDto dto);

    ApiResponse<?> registerByEmail(RegisterDto dto);

    ApiResponse<?> registerByPhone(RegisterByPhoneDto dto);

    ApiResponse<?> verify(VerifyDto dto);

    ApiResponse<?> verifyByPhone(VerifyByPhoneDto dto);

    ApiResponse<?> resendCodeToEmail(String email,String deviceId,Long id);

    ApiResponse<?> resendCodeToPhone(String phone, String deviceId, Long id);

    ApiResponse<?> checkCodeByEmail(String email, String code, String deviceId);

    ApiResponse<?> checkCodeByPhone(String phone, String code, String deviceId);

    ApiResponse<?> checkEmail(String email);

    ApiResponse<?> checkPhone(String phone);

    ApiResponse<?> forgetPasswordByEmail(String email, String deviceName);

    ApiResponse<?> forgetPasswordByPhone(String phone, String deviceName);

    ApiResponse<?> checkForgetPasswordByEmail(String email, String code, String deviceName , String newPassword);

    ApiResponse<?> checkForgetPasswordByPhone(String phone, String code, String deviceName, String newPassword);

}
