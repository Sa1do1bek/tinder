package com.example.tinder.service.firebase;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FirebaseAuthClient {


    public FirebaseToken verifyIdToken(String idToken) {
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (Exception e) {
            throw new IllegalStateException("ID token notog'ri yoki muddati o‘tgan", e);
        }
    }

    public FirebaseUserInfoResponse getUserInfo(String idToken) {
        try {
            FirebaseToken decoded = verifyIdToken(idToken);

            String uid = decoded.getUid();
            String email = decoded.getEmail();
            Boolean emailVerified = decoded.isEmailVerified();
            String fullName = decoded.getName();
            String picture = decoded.getPicture();

            Map<String, Object> claims = decoded.getClaims();

            String phoneNumber = null;
            if (claims.containsKey("phone_number")) {
                Object phoneObj = claims.get("phone_number");
                if (phoneObj != null) {
                    phoneNumber = phoneObj.toString();
                }
            }

            String signInProvider = null;
            Object firebaseObj = claims.get("firebase");
            if (firebaseObj instanceof Map<?, ?> firebaseMap) {
                Object providerObj = firebaseMap.get("sign_in_provider");
                if (providerObj != null) {
                    signInProvider = providerObj.toString();
                }
            }

            java.time.Instant issuedAt = null;
            if (claims.containsKey("iat")) {
                Object iatObj = claims.get("iat");
                if (iatObj instanceof Number num) {
                    issuedAt = issuedAt.plusSeconds(10);
                } else {
                    try {
                        issuedAt = issuedAt.plusSeconds(10);
                    } catch (Exception ignored) {
                    }
                }
            }

            java.time.Instant expiresAt = null;
            if (claims.containsKey("exp")) {
                Object expObj = claims.get("exp");
                if (expObj instanceof Number num) {
                    expiresAt = expiresAt.plusSeconds(10);
                } else {
                    try {
                        expiresAt = expiresAt.plusSeconds(10);
                    } catch (Exception ignored) {
                    }
                }
            }

            return FirebaseUserInfoResponse.builder()
                    .uid(uid)
                    .email(email)
                    .emailVerified(emailVerified)
                    .fullName(fullName)
                    .picture(picture)
                    .phoneNumber(phoneNumber)
                    .signInProvider(signInProvider)
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .claims(claims)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("Firebase ID token noto'g'ri yoki muddati o'tgan", e);
        }
    }
}
