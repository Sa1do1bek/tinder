package com.example.tinder.config;

import com.example.tinder.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;


    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return true; // SockJS fallback
        }


//        String uri = request.getURI().toString();

        // SockJS fallback routes - token talab qilinmaydi
//        if (uri.contains("/info") || uri.contains("iframe.html") ||
//                uri.contains("xhr") || uri.contains("jsonp") ||
//                uri.contains("eventsource")) {
//            return true;
//        }

        String token = servletRequest.getServletRequest().getParameter("token");

        if (token == null || token.isBlank()) {
            log.warn("❌ TOKEN YO'Q - HANDSHAKE BLOCKED");
            return false;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            attributes.put("username", username); // ✅ username sifatida saqlash
            log.info("✅ WebSocket connected: username={}", username);
            return true;
        } catch (Exception e) {
            log.error("❌ TOKEN XATO: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
