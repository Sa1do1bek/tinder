package com.example.tinder.service.firebase;

import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
import com.example.tinder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthUserRegistrationService oAuthUserRegistrationService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        log.info("🔵 OAuth2 login attempt for email: {}", email);

        processUser(email, name, picture);

        return oauth2User;
    }

    protected void processUser(String email, String fullname, String profilePic) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            log.info("✅ User already exists: {}", email);

            if (existingUser.getProvider() == AuthProvider.EMAIL) {
                log.info("🔄 Updating provider from LOCAL to LOCAL_AND_GOOGLE");
                existingUser.setProvider(AuthProvider.EMAIL_AND_GOOGLE);
                userRepository.save(existingUser);
            }

        } else {
            log.info("🆕 Creating new user: {}", email);

            String username = generateUniqueUsername(email);

            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .fullname(fullname)
                    .profilePic(profilePic)
                    .provider(AuthProvider.GOOGLE)
                    .subscriptionType(SubscriptionType.FREE)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.EVERYONE)
                    .active(true)
                    .isVerified(true)
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .freeTonight(false)
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .build();

            oAuthUserRegistrationService.register(newUser);

            log.info("✅ New user created successfully with all related entities");
        }
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        log.info("📝 Generated unique username: {}", username);
        return username;
    }
}