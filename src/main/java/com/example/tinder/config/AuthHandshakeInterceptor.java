package com.example.tinder.config;

import com.example.tinder.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return true; // SockJS fallback uchun token talab qilinmaydi
        }

        String uri = request.getURI().toString();

        // SockJS fallbacklar — token talab qilinmaydi
        if (uri.contains("/info") || uri.contains("iframe.html") ||
            uri.contains("xhr") || uri.contains("jsonp") ||
            uri.contains("eventsource")) {
            return true;
        }

        // Asosiy handshake so'rov
        String token = servletRequest.getServletRequest().getParameter("token");

        if (token == null || token.isBlank()) {
            System.out.println("TOKEN YO‘Q — HANDSHAKE BLOCK");
            return false;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            attributes.put("user", username);
            return true;
        } catch (Exception e) {
            System.out.println("TOKEN XATO: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
