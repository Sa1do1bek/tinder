package com.example.tinder.config.handler;

import com.example.tinder.entity.User;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.utils.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${app.oauth2.authorized-redirect-uris}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed");
            return;
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        
        log.info("OAuth2 login success for email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth2 login"));
        
        String token = jwtUtil.generateToken(user.getUsername());
        
        log.info("✅ Token generated for user: {}", user.getUsername());
        
        // Frontend ga redirect qilish
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri.split(",")[0])
                .queryParam("token", token)
                .queryParam("user", userMapper.toDto(user))
                .build().toUriString();
        
        log.info("Redirecting to: {}", targetUrl);
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}