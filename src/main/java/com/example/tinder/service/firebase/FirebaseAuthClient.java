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

    /**
     * Firebase ID tokenni verify qilish
     */
    public FirebaseToken verifyIdToken(String idToken) {
        try {
            log.info("🔐 Starting token verification...");

            // Token ni tozalash va validatsiya
            idToken = cleanAndValidateToken(idToken);

            // Firebase initialized ekanligini tekshirish
            if (FirebaseAuth.getInstance() == null) {
                log.error("❌ FirebaseAuth instance is null!");
                throw new IllegalStateException("Firebase not initialized");
            }

            log.info("✅ FirebaseAuth instance OK, verifying token...");

            // Token'ni verify qilish (bu yerda asosiy ishlov beriladi)
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);

            log.info("✅ Token verified successfully!");
            log.info("   UID: {}", token.getUid());
            log.info("   Email: {}", token.getEmail());
            log.info("   Name: {}", token.getName());
            log.info("   Issuer: {}", token.getIssuer());
            log.info("   Email Verified: {}", token.isEmailVerified());

            return token;

        } catch (FirebaseAuthException e) {
            log.error("❌ Firebase Auth Exception occurred!");
            log.error("   Error Code: {}", e.getAuthErrorCode());
            log.error("   Message: {}", e.getMessage());

            if (e.getHttpResponse() != null) {
                log.error("   HTTP Status: {}", e.getHttpResponse().getStatusCode());
                log.error("   HTTP Body: {}", e.getHttpResponse().getContent());
            }

            // Aniq error messages
            throw handleFirebaseAuthException(e);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid token format: {}", e.getMessage());
            throw new IllegalArgumentException("Token formati noto'g'ri: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌ Unexpected error during token verification", e);
            throw new IllegalStateException("Token verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Token'dan user ma'lumotlarini olish
     */
    public FirebaseUserInfoResponse getUserInfo(String idToken) {
        try {
            log.info("📝 Extracting user info from token...");

            FirebaseToken decoded = verifyIdToken(idToken);

            // Asosiy ma'lumotlar
            String uid = decoded.getUid();
            String email = decoded.getEmail();
            Boolean emailVerified = decoded.isEmailVerified();
            String fullName = decoded.getName();
            String picture = decoded.getPicture();

            Map<String, Object> claims = decoded.getClaims();
            log.debug("📋 Available claims: {}", claims.keySet());

            // Telefon raqami
            String phoneNumber = extractPhoneNumber(claims);

            // Sign-in provider (google, phone, password, etc.)
            String signInProvider = extractSignInProvider(claims);

            // Token vaqtlari
            Instant issuedAt = extractInstantFromClaim(claims, "iat");
            Instant expiresAt = extractInstantFromClaim(claims, "exp");

            log.info("✅ User info extracted successfully");
            log.info("   UID: {}", uid);
            log.info("   Email: {}", email);
            log.info("   Name: {}", fullName);
            log.info("   Provider: {}", signInProvider);
            log.info("   Phone: {}", phoneNumber);
            log.info("   Issued: {}", issuedAt);
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
            log.error("❌ Error extracting user info: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to extract user info from token", e);
        }
    }

    // ============== PRIVATE HELPER METHODS ==============

    /**
     * Token'ni tozalash va validatsiya qilish
     */
    private String cleanAndValidateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("❌ Token is null or empty");
            throw new IllegalArgumentException("ID token bo'sh bo'lishi mumkin emas");
        }

        // "Bearer " prefixni olib tashlash
        if (token.startsWith("Bearer ")) {
            log.debug("🧹 Removing 'Bearer ' prefix from token");
            token = token.substring(7);
        }

        // Bo'sh joylarni olib tashlash
        token = token.trim();

        log.debug("📏 Token length: {} characters", token.length());
        log.debug("🔍 Token preview: {}...", token.substring(0, Math.min(30, token.length())));

        // Token uzunligini tekshirish (Firebase token odatda 500-2000 char)
        if (token.length() < 100) {
            log.warn("⚠️ Token suspiciously short: {} chars", token.length());
        }

        return token;
    }

    /**
     * Firebase Auth Exception'ni handle qilish
     */
    private RuntimeException handleFirebaseAuthException(FirebaseAuthException e) {
        String errorCode = e.getAuthErrorCode().name();
        String message;

        switch (errorCode) {
            case "EXPIRED_ID_TOKEN":
                message = "Token muddati tugagan. Iltimos qayta login qiling.";
                break;
            case "INVALID_ID_TOKEN":
                message = "Token formati noto'g'ri yoki buzilgan.";
                break;
            case "REVOKED_ID_TOKEN":
                message = "Token bekor qilingan. Iltimos qayta login qiling.";
                break;
            case "USER_DISABLED":
                message = "Foydalanuvchi hisobi o'chirilgan yoki bloklangan.";
                break;
            case "USER_NOT_FOUND":
                message = "Foydalanuvchi topilmadi.";
                break;
            case "INVALID_CREDENTIAL":
                message = "Noto'g'ri autentifikatsiya ma'lumotlari.";
                break;
            default:
                message = "Token verification xatolik: " + e.getMessage();
        }

        return new IllegalStateException(message, e);
    }

    /**
     * Claims'dan telefon raqamini olish
     */
    private String extractPhoneNumber(Map<String, Object> claims) {
        if (claims.containsKey("phone_number")) {
            Object phoneObj = claims.get("phone_number");
            if (phoneObj != null) {
                return phoneObj.toString();
            }
        }
        return null;
    }

    /**
     * Sign-in provider'ni olish (google, phone, password, etc.)
     */
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

    /**
     * Claims'dan Instant vaqtni olish
     */
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