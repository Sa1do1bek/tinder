package com.example.tinder.log;

import com.example.tinder.annotation.LogUserAction;
import com.example.tinder.config.security.SecurityUtils;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLog;
import com.example.tinder.enums.UserAction;
import com.example.tinder.repository.UserLogRepository;
import com.example.tinder.service.UserLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UserLogAspect {

    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;
    private final UserLogService userLogService;

    @Around("@annotation(logUserAction)")
    public Object logUserAction(ProceedingJoinPoint joinPoint, LogUserAction logUserAction) throws Throwable {
        HttpServletRequest request = getCurrentRequest();

        if (request == null) {
            return joinPoint.proceed();
        }

        LocalDateTime requestTime = LocalDateTime.now();
        String requestBody = null;
        String responseBody = null;
        int statusCode = 200;
        String errorMessage = null;
        boolean success = true;
        SecurityUtils.CurrentUser currentUserForLog = securityUtils.getCurrentUserForLog()
                .orElse(null);

        if (logUserAction.logRequestBody()) {
            try {
                Object[] args = joinPoint.getArgs();
                requestBody = args.length > 0 ? objectMapper.writeValueAsString(args[0]) : null;
            } catch (Exception e) {
                log.warn("Failed to serialize request body", e);
            }
        }

        try {
            // Execute the method
            Object result = joinPoint.proceed();

            // Capture response body if needed
            if (logUserAction.logResponseBody()) {
                try {
                    responseBody = result != null ? objectMapper.writeValueAsString(result) : null;
                } catch (Exception e) {
                    log.warn("Failed to serialize response body", e);
                }
            }

            return result;

        } catch (Exception e) {
            success = false;
            statusCode = 500;
            errorMessage = e.getMessage();
            throw e;

        } finally {
            // Log the action
            LocalDateTime responseTime = LocalDateTime.now();
            UserLog userLog = buildUserLog(logUserAction.action().name(), currentUserForLog, requestTime, responseTime, success, statusCode, errorMessage, requestBody, responseBody);
            try {
                userLogService.save(userLog);
            } catch (Exception e) {
                log.error("Failed to save user log", e);
            }
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private UserLog buildUserLog(String action, SecurityUtils.CurrentUser currentUserForLog, LocalDateTime requestTime, LocalDateTime responseTime, boolean success, int statusCode, String errorMessage, String requestBody, String responseBody) {
        return UserLog.builder()
                .action(action)
                .userId(currentUserForLog != null ? currentUserForLog.id() : null)
                .username(currentUserForLog != null ? currentUserForLog.username() : "anonymous")
                .roles(currentUserForLog != null ? currentUserForLog.role() : List.of())
                .endpoint(securityUtils.getRequestUri())
                .ipAddress(securityUtils.getClientIp())
                .userAgent(securityUtils.getUserAgent())
                .requestTime(requestTime)
                .responseTime(responseTime)
                .statusCode(statusCode)
                .success(success)
                .errorMessage(errorMessage)
                .requestBody(requestBody)
                .responseBody(responseBody)
                .source("WEB")
                .build();
    }
}

