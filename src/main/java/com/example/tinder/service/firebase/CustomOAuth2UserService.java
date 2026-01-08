package com.example.tinder.service.firebase;

import com.example.tinder.entity.*;
import com.example.tinder.enums.AuthProvider;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthUserRegistrationService oAuthUserRegistrationService;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        
        processUser(email, name, picture);
        
        return oauth2User;
    }

    @Transactional
    protected void processUser(String email, String fullname, String profilePic) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            if (existingUser.getProvider() == AuthProvider.LOCAL) {
                    existingUser.setProvider(AuthProvider.LOCAL_AND_GOOGLE);
                    userRepository.save(existingUser);
            }
        } else {
            String username = generateUniqueUsername(email);
            
            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .fullname(fullname)
                    .profilePic(profilePic)
                    .provider(AuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.EVERYONE)
                    .active(true)
                    .isVerified(true)
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .build();

            oAuthUserRegistrationService.register(newUser);
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
        return username;
    }

}