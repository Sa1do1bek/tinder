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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserLocationRepository userLocationRepository;
    private final VerificationRepository verificationRepository;
    private final PreferenceRepository preferenceRepository;


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
    
    private void processUser(String email, String fullname, String profilePic) {
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
                    .active(true)
                    .isVerified(true)
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .build();

            User save = userRepository.save(newUser);
            saveCard(save);
            savePreference(save);
            saveVerification(save);
            saveUserLocation(save);
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

    public void saveUserLocation(User savedUser) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUser(savedUser);
        userLocation.setLatitude("41.1776056");
        userLocation.setLongitude("69.3562786");
        userLocation.setCreatedAt(LocalDateTime.now());
        userLocation.setUpdatedAt(LocalDateTime.now());
        userLocationRepository.save(userLocation);
    }

    public void saveVerification(User savedUser) {
        Verification verification = new Verification();
        verification.setUser(savedUser);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        verificationRepository.save(verification);
    }

    public void saveCard(User user) {
        Card card = new Card();
        card.setUserId(user.getId());
        card.setBalance(new BigDecimal("0.0"));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setTotalSuperLikesEarned(0);
        card.setSuperLikeCount(0);
        this.cardRepository.save(card);
    }

    public void savePreference(User user) {

        Preference preference = new Preference();
        preference.setUser(user);
        if (user.getGender() == Gender.MALE){
            preference.setPreferredGender(Gender.FEMALE);
        }else {
            preference.setPreferredGender(Gender.MALE);
        }
        preference.setDistance(70.0);
        preference.setMinAge(18);
        preference.setMaxAge(50);
        preference.setVisibility(true);

        preferenceRepository.save(preference);
    }
}