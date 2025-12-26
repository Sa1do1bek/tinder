package com.example.tinder.config;

import io.swagger.v3.oas.models.links.Link;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    @Getter
    private final Set<String> onlineUsers = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Map<String, Object> sessionAttributes = (Map<String, Object>) event.getMessage().getHeaders().get("simpSessionAttributes");
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("user");
            if (username != null) {
                onlineUsers.add(username);
                System.out.println("Online: " + username);
            }
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Map<String, Object> sessionAttributes = (Map<String, Object>) event.getMessage().getHeaders().get("simpSessionAttributes");
        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("user");
            if (username != null) {
                onlineUsers.remove(username);
                System.out.println("Offline: " + username);
            }
        }
    }

    public void isOnline(String username) {
        messagingTemplate.convertAndSend("/topic/chat/online" + username, onlineUsers.contains(username));
    }
}
