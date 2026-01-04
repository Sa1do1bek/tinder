package com.example.tinder.service.firebase;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class FirebaseAuthClient {

    public FirebaseToken verifyIdToken(String idToken) {
        try {
            log.info("🔐 Starting token verification...");
            log.debug("Token (first 50 chars): {}", idToken.substring(0, Math.min(50, idToken.length())));

            if (idToken == null || idToken.trim().isEmpty()) {
                log.error("❌ Token is null or empty");
                throw new IllegalArgumentException("ID token bo'sh");
            }

            // Check if Firebase is initialized
            if (FirebaseAuth.getInstance() == null) {
                log.error("❌ FirebaseAuth instance is null!");
                throw new IllegalStateException("Firebase not initialized");
            }

            log.info("✅ FirebaseAuth instance OK");

            // Verify token
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);

            log.info("✅ Token verified successfully!");
            log.info("   UID: {}", token.getUid());
            log.info("   Email: {}", token.getEmail());
            log.info("   Issuer: {}", token.getIssuer());

            return token;

        } catch (FirebaseAuthException e) {
            log.error("❌ Firebase Auth Exception!");
            log.error("   Error code: {}", e.getAuthErrorCode());
            log.error("   Message: {}", e.getMessage());
            log.error("   HTTP Response: {}", e.getHttpResponse());

            // Specific error messages
            switch (e.getAuthErrorCode().name()) {
                case "EXPIRED_ID_TOKEN":
                    throw new IllegalStateException("Token muddati o'tgan. Yangi token oling.", e);
                case "INVALID_ID_TOKEN":
                    throw new IllegalStateException("Token formati noto'g'ri.", e);
                case "REVOKED_ID_TOKEN":
                    throw new IllegalStateException("Token bekor qilingan.", e);
                case "USER_DISABLED":
                    throw new IllegalStateException("User o'chirilgan yoki disabled.", e);
                default:
                    throw new IllegalStateException("Firebase verification failed: " + e.getMessage(), e);
            }

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid argument: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Unexpected error during verification", e);
            throw new IllegalStateException("Token verification failed: " + e.getMessage(), e);
        }
    }

    public FirebaseUserInfoResponse getUserInfo(String idToken) {
        try {
            log.info("📝 Getting user info from token...");

            FirebaseToken decoded = verifyIdToken(idToken);

            String uid = decoded.getUid();
            String email = decoded.getEmail();
            Boolean emailVerified = decoded.isEmailVerified();
            String fullName = decoded.getName();
            String picture = decoded.getPicture();

            Map<String, Object> claims = decoded.getClaims();

            log.info("📋 Claims: {}", claims.keySet());

            // Phone number
            String phoneNumber = null;
            if (claims.containsKey("phone_number")) {
                phoneNumber = String.valueOf(claims.get("phone_number"));
            }

            // Sign-in provider
            String signInProvider = "unknown";
            Object firebaseObj = claims.get("firebase");
            if (firebaseObj instanceof Map<?, ?> firebaseMap) {
                Object providerObj = firebaseMap.get("sign_in_provider");
                if (providerObj != null) {
                    signInProvider = providerObj.toString();
                }
            }

            // Issued at
            Instant issuedAt = null;
            if (claims.containsKey("iat")) {
                Object iatObj = claims.get("iat");
                if (iatObj instanceof Number num) {
                    issuedAt = Instant.ofEpochSecond(num.longValue());
                }
            }

            // Expires at
            Instant expiresAt = null;
            if (claims.containsKey("exp")) {
                Object expObj = claims.get("exp");
                if (expObj instanceof Number num) {
                    expiresAt = Instant.ofEpochSecond(num.longValue());
                }
            }

            log.info("✅ User info extracted successfully");
            log.info("   UID: {}", uid);
            log.info("   Email: {}", email);
            log.info("   Provider: {}", signInProvider);
            log.info("   Expires: {}", expiresAt);

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
            log.error("❌ Error getting user info: {}", e.getMessage(), e);
            throw new IllegalStateException("Firebase token verification failed", e);
        }
    }
}