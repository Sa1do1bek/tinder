package com.example.tinder.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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
        System.out.println("JwtUtil.generateToken called with username: " + username);

        try {
            if (username == null || username.trim().isEmpty()) {
                System.out.println("Username is null or empty - returning null");
                return null;
            }

            Map<String, Object> claims = new HashMap<>();
            String token = createToken(claims, username);

            System.out.println("Token created: " + (token != null ? "SUCCESS" : "FAILED"));
            if (token != null) {
                System.out.println("Token length: " + token.length());
                System.out.println("Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");
            }

            return token;
        } catch (Exception e) {
            System.out.println("Error in generateToken: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        System.out.println("Creating token for subject: " + subject);

        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10 * 24)) // 10 kun
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS512)
                    .compact();

            System.out.println("Token created successfully in createToken method");
            return token;
        } catch (Exception e) {
            System.out.println("Error in createToken: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Error validating token: ", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Error extracting username: ", e);
            return null;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .setAllowedClockSkewSeconds(30)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        try {
            return extractAllClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: ", e);
            return true;
        }
    }

    public String getSecretKeyAsString() {
        return Base64.getEncoder().encodeToString(SECRET_KEY.getEncoded());
    }
}
