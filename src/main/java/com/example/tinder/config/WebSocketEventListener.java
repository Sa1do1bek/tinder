package com.example.tinder.config;

import com.example.tinder.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    @Getter
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>(); // username -> sessionId

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();   // 👈 shu

        if (principal != null) {

            String username = principal.getName();// 👈 username
            String sessionId = accessor.getSessionId();
            if (username != null && sessionId != null) {

                onlineUsers.put(username, sessionId);
                log.info("🟢 Online: username={}, sessionId={}", username, sessionId);

                // Broadcast online status
                messagingTemplate.convertAndSend(
                        "/queue/" + username + "/status",
                        Map.of(
                                "username", username,
                                "online", true,
                                "timestamp", System.currentTimeMillis()
                        )
                );
                userRepository.updateOnlineStatus(username, true, null);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();

        if (attributes != null) {
            String username = (String) attributes.get("username");

            if (username != null) {
                onlineUsers.remove(username);
                log.info("🔴 Offline: username={}", username);


                userRepository.updateOnlineStatus(
                        username,
                        false,
                        LocalDateTime.now()
                );

                // Broadcast offline status
                messagingTemplate.convertAndSend(
                        "/queue/" + username + "/status",
                        Map.of(
                                "username", username,
                                "online", false,
                                "timestamp", System.currentTimeMillis()
                        ));
            }
        }
    }

    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public Set<String> getOnlineUsernames() {
        return new HashSet<>(onlineUsers.keySet());
    }
}
