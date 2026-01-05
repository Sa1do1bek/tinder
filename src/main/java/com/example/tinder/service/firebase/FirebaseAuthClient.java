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
        log.info("=================================================");
        log.info("🔐 STARTING TOKEN VERIFICATION");
        log.info("=================================================");

        try {
            // Token ni tozalash va validatsiya
            idToken = cleanAndValidateToken(idToken);

            log.info("📋 Checking Firebase initialization...");

            // Firebase initialized ekanligini tekshirish
            if (com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                log.error("❌ Firebase is NOT initialized!");
                throw new IllegalStateException("Firebase not initialized. Check FirebaseConfig.");
            }

            log.info("✅ Firebase is initialized");
            log.info("   App name: {}", com.google.firebase.FirebaseApp.getInstance().getName());
            log.info("   Project ID: {}", com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());

            log.info("🔍 Verifying token with Firebase...");

            // Token'ni verify qilish (bu yerda asosiy ishlov beriladi)
            FirebaseToken token = FirebaseAuth.getInstance().verifyIdToken(idToken);

            log.info("✅✅✅ TOKEN VERIFIED SUCCESSFULLY! ✅✅✅");
            log.info("   UID: {}", token.getUid());
            log.info("   Email: {}", token.getEmail());
            log.info("   Name: {}", token.getName());
            log.info("   Email Verified: {}", token.isEmailVerified());
            log.info("   Issuer: {}", token.getIssuer());
            log.info("=================================================");

            return token;

        } catch (FirebaseAuthException e) {
            log.error("=================================================");
            log.error("❌❌❌ FIREBASE AUTH EXCEPTION ❌❌❌");
            log.error("=================================================");
            log.error("Error Code: {}", e.getAuthErrorCode());
            log.error("Error Message: {}", e.getMessage());
            log.error("Error Class: {}", e.getClass().getName());

            if (e.getHttpResponse() != null) {
                log.error("HTTP Status: {}", e.getHttpResponse().getStatusCode());
                log.error("HTTP Body: {}", e.getHttpResponse().getContent());
            }

            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }

            log.error("=================================================");

            throw handleFirebaseAuthException(e);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid token format: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token formati noto'g'ri: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("❌❌❌ UNEXPECTED ERROR ❌❌❌");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            throw new IllegalStateException("Token verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Token'dan user ma'lumotlarini olish
     */
    public FirebaseUserInfoResponse getUserInfo(String idToken) {
        log.info("=================================================");
        log.info("📝 EXTRACTING USER INFO FROM TOKEN");
        log.info("=================================================");

        try {
            log.info("Step 1: Verifying token...");
            FirebaseToken decoded = verifyIdToken(idToken);

            log.info("Step 2: Token verified, extracting claims...");

            // Asosiy ma'lumotlar
            String uid = decoded.getUid();
            String email = decoded.getEmail();
            Boolean emailVerified = decoded.isEmailVerified();
            String fullName = decoded.getName();
            String picture = decoded.getPicture();

            log.info("Step 3: Basic info extracted:");
            log.info("   UID: {}", uid);
            log.info("   Email: {}", email);
            log.info("   Name: {}", fullName);
            log.info("   Email Verified: {}", emailVerified);

            Map<String, Object> claims = decoded.getClaims();
            log.info("Step 4: Processing claims...");
            log.info("   Total claims: {}", claims.size());
            log.info("   Available claim keys: {}", claims.keySet());

            // Telefon raqami
            String phoneNumber = extractPhoneNumber(claims);
            log.info("   Phone number: {}", phoneNumber);

            // Sign-in provider (google, phone, password, etc.)
            String signInProvider = extractSignInProvider(claims);
            log.info("   Sign-in provider: {}", signInProvider);

            // Token vaqtlari
            Instant issuedAt = extractInstantFromClaim(claims, "iat");
            Instant expiresAt = extractInstantFromClaim(claims, "exp");
            log.info("   Issued at: {}", issuedAt);
            log.info("   Expires at: {}", expiresAt);

            log.info("✅ USER INFO EXTRACTION SUCCESSFUL");
            log.info("=================================================");

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
            log.error("❌ Token verification failed in getUserInfo");
            log.error("   Error: {}", e.getMessage(), e);
            throw e; // Re-throw the same exception

        } catch (Exception e) {
            log.error("❌❌❌ ERROR EXTRACTING USER INFO ❌❌❌");
            log.error("   Error type: {}", e.getClass().getName());
            log.error("   Error message: {}", e.getMessage());
            log.error("   Stack trace:", e);
            throw new IllegalStateException("Failed to extract user info from token: " + e.getMessage(), e);
        }
    }

    // ============== PRIVATE HELPER METHODS ==============

    /**
     * Token'ni tozalash va validatsiya qilish
     */
    private String cleanAndValidateToken(String token) {
        log.info("🧹 Cleaning and validating token...");

        if (token == null || token.trim().isEmpty()) {
            log.error("❌ Token is null or empty!");
            throw new IllegalArgumentException("ID token bo'sh bo'lishi mumkin emas");
        }

        String originalLength = String.valueOf(token.length());
        log.info("   Original length: {} chars", originalLength);

        // "Bearer " prefixni olib tashlash
        if (token.startsWith("Bearer ")) {
            log.info("   Removing 'Bearer ' prefix");
            token = token.substring(7);
        }

        // Bo'sh joylarni olib tashlash
        token = token.trim();

        log.info("   Final length: {} chars", token.length());
        log.info("   Token preview (first 50 chars): {}",
                token.substring(0, Math.min(50, token.length())));
        log.info("   Token preview (last 50 chars): {}",
                token.substring(Math.max(0, token.length() - 50)));

        // Token uzunligini tekshirish
        if (token.length() < 100) {
            log.warn("⚠️ Token suspiciously short: {} chars", token.length());
        } else if (token.length() > 3000) {
            log.warn("⚠️ Token suspiciously long: {} chars", token.length());
        } else {
            log.info("✅ Token length looks valid");
        }

        // Token formatini tekshirish (JWT format: xxx.yyy.zzz)
        int dotCount = token.length() - token.replace(".", "").length();
        log.info("   Dot count in token: {}", dotCount);

        if (dotCount != 2) {
            log.warn("⚠️ Token doesn't look like a JWT (expected 2 dots, found {})", dotCount);
        }

        return token;
    }

    /**
     * Firebase Auth Exception'ni handle qilish
     */
    private RuntimeException handleFirebaseAuthException(FirebaseAuthException e) {
        String errorCode = e.getAuthErrorCode().name();
        String message;

        log.error("🔍 Analyzing Firebase error code: {}", errorCode);

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
            case "PROJECT_NOT_FOUND":
                message = "Firebase project topilmadi. Server konfiguratsiyasini tekshiring.";
                break;
            default:
                message = "Token verification xatolik: " + e.getMessage();
        }

        log.error("📢 User-friendly message: {}", message);
        return new IllegalStateException(message, e);
    }

    /**
     * Claims'dan telefon raqamini olish
     */
    private String extractPhoneNumber(Map<String, Object> claims) {
        try {
            if (claims.containsKey("phone_number")) {
                Object phoneObj = claims.get("phone_number");
                if (phoneObj != null) {
                    log.debug("   Found phone_number in claims: {}", phoneObj);
                    return phoneObj.toString();
                }
            }
            log.debug("   No phone_number in claims");
        } catch (Exception e) {
            log.warn("⚠️ Error extracting phone number: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Sign-in provider'ni olish (google, phone, password, etc.)
     */
    private String extractSignInProvider(Map<String, Object> claims) {
        try {
            Object firebaseObj = claims.get("firebase");
            log.debug("   Firebase claim object: {}", firebaseObj);

            if (firebaseObj instanceof Map<?, ?> firebaseMap) {
                Object providerObj = firebaseMap.get("sign_in_provider");
                if (providerObj != null) {
                    log.debug("   Found sign_in_provider: {}", providerObj);
                    return providerObj.toString();
                }
            }
            log.debug("   No sign_in_provider found, using 'unknown'");
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
                log.debug("   {} claim value: {}", claimKey, claimObj);

                if (claimObj instanceof Number num) {
                    return Instant.ofEpochSecond(num.longValue());
                }
            }
            log.debug("   {} claim not found", claimKey);
        } catch (Exception e) {
            log.warn("⚠️ Could not extract {} from claims: {}", claimKey, e.getMessage());
        }
        return null;
    }
}