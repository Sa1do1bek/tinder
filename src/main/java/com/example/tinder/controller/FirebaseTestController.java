package com.example.tinder.controller;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.example.tinder.service.firebase.FirebaseAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test/firebase")
@RequiredArgsConstructor
public class FirebaseTestController {

    private final FirebaseAuthClient firebaseAuthClient;

    /**
     * Token'ni test qilish (Authorization header orqali)
     */
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String authHeader) {
        try {
            log.info("📥 Received verification request");
            log.info("   Auth header preview: {}...", 
                authHeader.substring(0, Math.min(50, authHeader.length())));

            // Token'ni olish
            String token = extractToken(authHeader);
            
            log.info("🔍 Extracted token:");
            log.info("   Length: {} characters", token.length());
            log.info("   Preview: {}...", token.substring(0, Math.min(50, token.length())));

            // Verify qilish
            FirebaseUserInfoResponse userInfo = firebaseAuthClient.getUserInfo(token);

            // Response tayyorlash
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Token successfully verified");
            response.put("user", Map.of(
                "uid", userInfo.getUid(),
                "email", userInfo.getEmail() != null ? userInfo.getEmail() : "N/A",
                "name", userInfo.getFullName() != null ? userInfo.getFullName() : "N/A",
                "emailVerified", userInfo.getEmailVerified(),
                "provider", userInfo.getSignInProvider(),
                "phoneNumber", userInfo.getPhoneNumber() != null ? userInfo.getPhoneNumber() : "N/A",
                "picture", userInfo.getPicture() != null ? userInfo.getPicture() : "N/A"
            ));
            response.put("tokenInfo", Map.of(
                "issuedAt", userInfo.getIssuedAt(),
                "expiresAt", userInfo.getExpiresAt()
            ));

            log.info("✅ Verification successful for UID: {}", userInfo.getUid());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid token format: {}", e.getMessage());
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Token formati noto'g'ri", e.getMessage());
            
        } catch (IllegalStateException e) {
            log.error("❌ Token verification failed: {}", e.getMessage());
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Token verification failed", e.getMessage());
            
        } catch (Exception e) {
            log.error("❌ Unexpected error: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server xatosi", e.getMessage());
        }
    }

    /**
     * Token'ni test qilish (Request body orqali)
     */
    @PostMapping("/verify-token-body")
    public ResponseEntity<?> verifyTokenFromBody(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("idToken");
            
            if (token == null || token.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, 
                    "Token topilmadi", "Request body'da 'idToken' field bo'lishi kerak");
            }

            log.info("📥 Received token from body");
            log.info("   Length: {} characters", token.length());

            // Verify qilish
            FirebaseUserInfoResponse userInfo = firebaseAuthClient.getUserInfo(token);

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("uid", userInfo.getUid());
            response.put("email", userInfo.getEmail());
            response.put("name", userInfo.getFullName());
            response.put("provider", userInfo.getSignInProvider());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Verification failed: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "Verification failed", e.getMessage());
        }
    }

    /**
     * Firebase status tekshirish
     */
    @GetMapping("/status")
    public ResponseEntity<?> getFirebaseStatus() {
        try {
            com.google.firebase.FirebaseApp app = com.google.firebase.FirebaseApp.getInstance();
            
            Map<String, Object> status = new HashMap<>();
            status.put("initialized", true);
            status.put("appName", app.getName());
            status.put("projectId", app.getOptions().getProjectId());
            
            log.info("✅ Firebase status check successful");
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("❌ Firebase not initialized: {}", e.getMessage());
            
            Map<String, Object> status = new HashMap<>();
            status.put("initialized", false);
            status.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }

    // ============== HELPER METHODS ==============

    /**
     * Authorization header'dan token'ni olish
     */
    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization header bo'sh");
        }

        // "Bearer " ni olib tashlash
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // Agar Bearer bo'lmasa, to'g'ridan-to'g'ri token deb hisoblaymiz
        return authHeader.trim();
    }

    /**
     * Error response yaratish
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("details", details);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(status).body(response);
    }
}