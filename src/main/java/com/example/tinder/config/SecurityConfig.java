package com.example.tinder.config;

import com.example.tinder.filter.JwtFilter;
import com.example.tinder.service.firebase.CustomOAuth2UserService;
import com.example.tinder.service.firebase.OAuth2AuthenticationFailureHandler;
import com.example.tinder.service.firebase.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    private final static String[] WHITE_LIST = {
            "/swagger-ui/**",
            "/auths/forgetPasswordByPhone",
            "/auths/forgetPasswordByEmail",
            "/v3/api-docs/**",
            "/users/checkUsername/**",
            "/swagger-ui.html",
            "/auths/**",
            "/api/v1/auth/**",      // Firebase auth uchun
            "/oauth2/**",           // Google OAuth2 uchun
            "/login/oauth2/**",     // OAuth2 login callback
            "/ws/**",               // WebSocket endpoint
            "/ws",                  // WebSocket root
            "/conversation/**",
            "/chat/**",
            "/app/**",              // STOMP destination prefix
            "/topic/**",            // STOMP topic
            "/queue/**" ,            // STOMP queue
            "/api/payment/perpare", // Payment
            "/api/payment/complete", // Payment
            "/api/payment/click/prepare", // Payment alias
            "/api/payment/click/complete" // Payment alias
    };

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize"))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/*"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint));


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins
        configuration.setAllowedOrigins(List.of(
                "http://localhost:63342",
                "http://localhost:3000",
                "http://localhost:8080",
                "https://semispheric-paloma-factiously.ngrok-free.dev",
                "http://192.168.7.223/:8081", // here
                "http://192.168.7.100:3000", // here
                "http://localhost:8081",
                "https://api.tinder.tenzorsoft.uz",
                "https://api-tinder.tenzorsoft.uz",
                "https://front-tinder.tenzorsoft.uz",
                "https://luvi.uz",
                "ws://localhost:8080",
                "ws://localhost:8081",
                "ws://api-tinder.tenzorsoft.uz",
                "wss://api-tinder.tenzorsoft.uz"
        ));

        // Allow credentials
        configuration.setAllowCredentials(true);

        // Allowed methods
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(List.of("*"));

        // Exposed headers
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept"
        ));

        // Max age
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
