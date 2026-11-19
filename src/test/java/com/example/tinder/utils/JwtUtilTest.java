package com.example.tinder.utils;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    @Test
    void signsAndVerifiesWithConfiguredKey() {
        JwtUtil jwt = new JwtUtil(Base64.getEncoder().encodeToString(new byte[64]));
        String token = jwt.generateToken("user@example.com");

        assertTrue(jwt.validateToken(token, "user@example.com"));
        assertFalse(jwt.validateToken(token, "someone@example.com"));
    }

    @Test
    void rejectsWeakSigningKeys() {
        String weakKey = Base64.getEncoder().encodeToString(new byte[32]);
        assertThrows(IllegalArgumentException.class, () -> new JwtUtil(weakKey));
    }
}
