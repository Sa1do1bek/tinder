package com.example.tinder.config.security;


import com.example.tinder.entity.User;
import com.example.tinder.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final HttpServletRequest request;

    public Optional<CurrentUser> getCurrentUserForLog() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof User user) {
            List<String> roles = user.getRole().stream().map(UserRole::getName).toList();
            return Optional.of(new CurrentUser(user.getId(), user.getUsername(), roles));
        }

        return Optional.of(new CurrentUser(null, authentication.getName(), List.of()));
    }

    public Optional<Long> getCurrentUserIdForLog() {
        return getCurrentUserForLog().map(CurrentUser::id);
    }

    public String getClientIp() {
        try {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = request.getRemoteAddr();
            }
            return ip;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getRequestUri() {
        try {
            return request.getRequestURI();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String getUserAgent() {
        try {
            return request.getHeader("User-Agent");
        } catch (Exception e) {
            return "unknown";
        }
    }


    public record CurrentUser(Long id, String username, List<String> role) {
    }
}