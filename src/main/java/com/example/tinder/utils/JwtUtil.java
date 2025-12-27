package com.example.tinder.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {

    private static final String KEY = "REMOVED";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(KEY));

    public String generateToken(String username) {
        log.debug("Generating token for username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            log.error("Username is null or empty");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            String token = createToken(claims, username);
            log.info("✅ Token generated successfully for user: {}", username);
            return token;
        } catch (Exception e) {
            log.error("❌ Error generating token", e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        try {
            Date now = new Date();
            Date expiration = new Date(now.getTime() + (1000L * 60 * 60 * 24 * 10)); // 10 days

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                    .compact();

            log.debug("Token created: length={}, exp={}", token.length(), expiration);
            return token;
        } catch (Exception e) {
            log.error("❌ Error creating token", e);
            throw new RuntimeException("Token creation failed", e);
        }
    }

    public Boolean validateToken(String token, String username) {
        if (token == null || username == null) {
            log.warn("⚠️ Token or username is null");
            return false;
        }

        try {
            String extractedUsername = extractUsername(token);
            boolean isValid = username.equals(extractedUsername) && !isTokenExpired(token);

            if (!isValid) {
                log.warn("⚠️ Token validation failed for user: {}", username);
            }

            return isValid;
        } catch (Exception e) {
            log.error("❌ Error validating token", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("❌ Token is null or empty");
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            Claims claims = extractAllClaims(token);
            String username = claims.getSubject();
            log.debug("✅ Extracted username: {}", username);
            return username;
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token expired: {}", e.getMessage());
            throw new RuntimeException("Token has expired", e);
        } catch (Exception e) {
            log.error("❌ Error extracting username from token", e);
            throw new RuntimeException("Failed to extract username from token", e);
        }
    }

    public Claims extractAllClaims(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .setAllowedClockSkewSeconds(30)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("⚠️ Token has expired");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("❌ Malformed JWT token", e);
            throw new RuntimeException("Invalid token format", e);
        } catch (SignatureException e) {
            log.error("❌ Invalid JWT signature", e);
            throw new RuntimeException("Invalid token signature", e);
        } catch (Exception e) {
            log.error("❌ Error parsing JWT token", e);
            throw new RuntimeException("Token parsing failed", e);
        }
    }

    public Boolean isTokenExpired(String token) {
        if (token == null || token.trim().isEmpty()) {
            return true;
        }

        try {
            Date expiration = extractAllClaims(token).getExpiration();
            boolean expired = expiration.before(new Date());

            if (expired) {
                log.debug("Token expired at: {}", expiration);
            }

            return expired;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("❌ Error checking token expiration", e);
            return true;
        }
    }

    public String getSecretKeyAsString() {
        return Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
    }
}
