package com.example.tinder.exception.handler;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.exception.RestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Map;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class WebSocketExceptionHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageExceptionHandler(RestException.class)
    public void handleException(RestException e, @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {
        String username = (String) sessionAttributes.get("username");

        if (username != null) {
            log.error("WebSocket Error for user {}: {}", username, e.getErrorType().getMsg());

            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/feedback",
                    ApiResponse.error(e.getErrorType().getMsg())
            );
        }
    }
}