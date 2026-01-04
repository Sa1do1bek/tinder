package com.example.tinder.service.impl;

import com.example.tinder.entity.User;
import com.example.tinder.enums.AuthProvider;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        log.info("OAuth2 User attributes: {}", oauth2User.getAttributes());
        
        // Google-dan kelgan ma'lumotlar
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        
        log.info("Processing OAuth2 user - Email: {}, Name: {}", email, name);
        
        // User ni DB ga saqlash yoki yangilash
        try {
            processUser(email, name, picture);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return oauth2User;
    }
    
    private void processUser(String email, String fullname, String profilePic) throws ParseException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            // User mavjud - yangilash
            User existingUser = userOptional.get();
            log.info("Existing user found: {}", existingUser.getUsername());
            
            existingUser.setFullname(fullname);
            existingUser.setProfilePic(profilePic);
            userRepository.save(existingUser);
            
        } else {
            log.info("Creating new user for email: {}", email);
            
            String username = generateUniqueUsername(email);
            
            User newUser = User.builder()
                    .email(email)
                    .username(username)
                    .fullname(fullname)
                    .profilePic(profilePic)
                    .provider(AuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .active(true)
                    .isVerified(true)  // Google-dan kelgan verified
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .build();
            
            userRepository.save(newUser);
            log.info("✅ New user created: {}", username);
        }
    }
    
    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}