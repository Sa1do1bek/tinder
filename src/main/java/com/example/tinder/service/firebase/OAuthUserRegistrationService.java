package com.example.tinder.service.firebase;

import com.example.tinder.entity.*;
import com.example.tinder.enums.Gender;
import com.example.tinder.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OAuthUserRegistrationService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PreferenceRepository preferenceRepository;
    private final VerificationRepository verificationRepository;
    private final UserLocationRepository userLocationRepository;

    @Transactional
    public void register(User user) {
        User saved = userRepository.save(user);
        savePreference(saved);
        saveCard(saved);
        saveVerification(saved);
        saveUserLocation(saved);
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
