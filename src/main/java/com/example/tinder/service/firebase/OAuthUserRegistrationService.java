package com.example.tinder.service.firebase;

import com.example.tinder.entity.*;
import com.example.tinder.enums.Gender;
import com.example.tinder.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthUserRegistrationService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PreferenceRepository preferenceRepository;
    private final VerificationRepository verificationRepository;
    private final UserLocationRepository userLocationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(User user) {
        log.info("📝 Starting OAuth user registration...");
        User savedUser = userRepository.save(user);
        log.info("✅ User saved with ID: {}", savedUser.getId());

        try {
            log.info("✅ UserLocation save ga kirdi ");
            saveUserLocation(savedUser);
            log.info("✅ UserLocation saved for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("❌ Error saving UserLocation: {}", e.getMessage(), e);
            throw e;
        }

        try {
            log.info("✅ Card save ga kirdi ");
            saveCard(savedUser);
            log.info("✅ Card saved for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("❌ Error saving Card: {}", e.getMessage(), e);
            throw e;
        }

        try {
            log.info("✅ Verification save ga kirdi ");
            saveVerification(savedUser);
            log.info("✅ Verification saved for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("❌ Error saving Verification: {}", e.getMessage(), e);
            throw e;
        }

        try {
            log.info("✅ Preference save ga kirdi ");
            savePreference(savedUser);
            log.info("✅ Preference saved for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            log.error("❌ Error saving Preference: {}", e.getMessage(), e);
            throw e;
        }

        log.info("🎉 OAuth user registration completed successfully!");
    }

    private void saveUserLocation(User savedUser) {
        UserLocation userLocation = UserLocation.builder()
                .user(savedUser)
                .latitude("41.1776056")
                .longitude("69.3562786")
                .build();

        userLocation.setCreatedAt(LocalDateTime.now());
        userLocation.setUpdatedAt(LocalDateTime.now());

        userLocationRepository.save(userLocation);
    }

    private void saveVerification(User savedUser) {
        Verification verification = Verification.builder()
                .user(savedUser)
                .similarityScore(0.0)
                .build();

        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());

        verificationRepository.save(verification);
    }

    private void saveCard(User savedUser) {
        Card card = Card.builder()
                .user(savedUser)
                .balance(new BigDecimal("0.0"))
                .superLikeCount(0)
                .number(UUID.randomUUID().toString())
                .totalSuperLikesEarned(0)
                .build();

        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        cardRepository.save(card);
    }

    private void savePreference(User savedUser) {
        Preference preference = Preference.builder()
                .user(savedUser)
                .preferredGender(getPreferredGender(savedUser.getGender()))
                .distance(70.0)
                .minAge(18)
                .maxAge(50)
                .build();

        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());

        preferenceRepository.save(preference);

    }

    private Gender getPreferredGender(Gender userGender){
        if (userGender == Gender.MALE) {
            return Gender.FEMALE;
        } else if (userGender == Gender.FEMALE) {
            return Gender.MALE;
        } else {
            return Gender.EVERYONE;
        }
    }
}