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
            idToken = cleanAndValidateToken(idToken);

            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                throw new IllegalStateException("Firebase not initialized. Check FirebaseConfig.");
            }

            return FirebaseAuth.getInstance().verifyIdToken(idToken, true);

        } catch (FirebaseAuthException e) {
            throw handleFirebaseAuthException(e);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token formati noto'g'ri: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new IllegalStateException("Token verification failed: " + e.getMessage(), e);
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

            String phoneNumber = extractPhoneNumber(claims);

            String signInProvider = extractSignInProvider(claims);

            Instant issuedAt = extractInstantFromClaim(claims, "iat");
            Instant expiresAt = extractInstantFromClaim(claims, "exp");

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

        } catch (IllegalStateException e) {
            throw e;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to extract user info from token: " + e.getMessage(), e);
        }
    }

    private String cleanAndValidateToken(String token) {

        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("ID token bo'sh bo'lishi mumkin emas");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        token = token.trim();

        return token;
    }

    private RuntimeException handleFirebaseAuthException(FirebaseAuthException e) {
        String errorCode = e.getAuthErrorCode().name();
        String message = switch (errorCode) {
            case "EXPIRED_ID_TOKEN" -> "Token muddati tugagan. Iltimos qayta login qiling.";
            case "INVALID_ID_TOKEN" -> "Token formati noto'g'ri yoki buzilgan.";
            case "REVOKED_ID_TOKEN" -> "Token bekor qilingan. Iltimos qayta login qiling.";
            case "USER_DISABLED" -> "Foydalanuvchi hisobi o'chirilgan yoki bloklangan.";
            case "USER_NOT_FOUND" -> "Foydalanuvchi topilmadi.";
            case "INVALID_CREDENTIAL" -> "Noto'g'ri autentifikatsiya ma'lumotlari.";
            case "PROJECT_NOT_FOUND" -> "Firebase project topilmadi. Server konfiguratsiyasini tekshiring.";
            default -> "Token verification xatolik: " + e.getMessage();
        };
        return new IllegalStateException(message, e);
    }

    private String extractPhoneNumber(Map<String, Object> claims) {
        try {
            if (claims.containsKey("phone_number")) {
                Object phoneObj = claims.get("phone_number");
                if (phoneObj != null) {
                    return phoneObj.toString();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Error extracting phone number: {}", e.getMessage());
        }
        return null;
    }


    private String extractSignInProvider(Map<String, Object> claims) {
        try {
            Object firebaseObj = claims.get("firebase");

            if (firebaseObj instanceof Map<?, ?> firebaseMap) {
                Object providerObj = firebaseMap.get("sign_in_provider");
                if (providerObj != null) {
                    return providerObj.toString();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not extract sign-in provider: {}", e.getMessage());
        }
        return "unknown";
    }


    private Instant extractInstantFromClaim(Map<String, Object> claims, String claimKey) {
        try {
            if (claims.containsKey(claimKey)) {
                Object claimObj = claims.get(claimKey);

                if (claimObj instanceof Number num) {
                    return Instant.ofEpochSecond(num.longValue());
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not extract {} from claims: {}", claimKey, e.getMessage());
        }
        return null;
    }
}