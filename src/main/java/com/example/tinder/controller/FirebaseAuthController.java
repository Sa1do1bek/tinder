package com.example.tinder.controller;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.example.tinder.dto.auth.GoogleLoginRequest;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.service.firebase.FirebaseAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class FirebaseAuthController {

    private final FirebaseAuthService authService;

    public FirebaseAuthController(FirebaseAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public ApiResponse<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request.getIdToken());
    }

    @PostMapping("/google-info")
    public ResponseEntity<FirebaseUserInfoResponse> getUserInfo(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.getUserInfo(request.getIdToken()));
    }
}