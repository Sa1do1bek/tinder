package com.example.tinder.config.security;

import com.example.tinder.annotation.LogUserAction;
import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.UserAction;
import com.example.tinder.service.UserService;
import com.example.tinder.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public Oauth2LoginSuccessHandler(UserService userService, ObjectMapper objectMapper1) {
        this.userService = userService;
        this.objectMapper = objectMapper1;
    }

    @LogUserAction(action = UserAction.LOGIN, description = "LOGIN OR REGISTER WITH OAUTH")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = token.getPrincipal();

        String email = (String) oauthUser.getAttributes().get("email");
        String name = (String) oauthUser.getAttributes().get("name");
        String profilePic = (String) oauthUser.getAttributes().get("picture");

        ApiResponse apiResponse = userService.googleAuth(email, name, profilePic);

        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
