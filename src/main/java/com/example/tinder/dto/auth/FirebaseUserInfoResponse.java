package com.example.tinder.dto.auth;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FirebaseUserInfoResponse {

    private String uid;
    private String email;
    private Boolean emailVerified;
    private String fullName;
    private String picture;
    private String phoneNumber;
    private String signInProvider;

    private Instant issuedAt;   // iat
    private Instant expiresAt;  // exp

    private Map<String, Object> claims;
}