package com.example.tinder.service.firebase;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.AuthProvider;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseAuthService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final FirebaseAuthClient firebaseAuthClient;

    @Transactional
    public ApiResponse<?> loginWithGoogle(String idToken) {

        FirebaseUserInfoResponse firebaseUser = firebaseAuthClient.getUserInfo(idToken);

        String firebaseUid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getFullName();
        String phoneNumber = firebaseUser.getPhoneNumber();
        Boolean emailVerified = firebaseUser.getEmailVerified();
        String picture = firebaseUser.getPicture();

        if (firebaseUid == null || firebaseUid.isBlank()) {
            throw new IllegalStateException("Firebase UID topilmadi");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email topilmadi");
        }

        log.info("Firebase login - UID: {}, Email: {}", firebaseUid, email);

        Optional<User> optionalUser = userRepository.findByFirebaseUidOrEmail(firebaseUid, email);

        User user;
        boolean isNewUser = false;

        if (optionalUser.isEmpty()) {
            // YANGI USER
            isNewUser = true;
            log.info("Creating NEW user for Firebase UID: {}", firebaseUid);

            String username = generateUniqueUsername(email);

            user = User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email)
                    .username(username)  // MUHIM!
                    .fullname(name != null && !name.isBlank() ? name : "User")
                    .profilePic(picture)
                    .isVerified(emailVerified != null ? emailVerified : false)
                    .status(UserStatus.ACTIVE)
                    .role(UserRole.USER)
                    .provider(AuthProvider.GOOGLE)
                    .active(true)
                    .showProfile(true)
                    .isOnline(false)
                    .isApproved(false)
                    .build();

            user = userRepository.save(user);
            log.info("✅ New user created: {} (ID: {})", username, user.getId());

        } else {
            // MAVJUD USER
            user = optionalUser.get();
            log.info("Existing user found: {} (ID: {})", user.getUsername(), user.getId());

            boolean needsUpdate = false;

            // Firebase UID ni yangilash
            if (user.getFirebaseUid() == null || !firebaseUid.equals(user.getFirebaseUid())) {
                user.setFirebaseUid(firebaseUid);
                needsUpdate = true;
            }

            // Email ni yangilash
            if (!email.equalsIgnoreCase(user.getEmail())) {
                user.setEmail(email);
                needsUpdate = true;
            }

            // Fullname ni yangilash
            if (name != null && !name.isBlank() &&
                    (user.getFullname() == null || !name.equals(user.getFullname()))) {
                user.setFullname(name);
                needsUpdate = true;
            }

            // Profile picture ni yangilash
            if (picture != null && !picture.equals(user.getProfilePic())) {
                user.setProfilePic(picture);
                needsUpdate = true;
            }

            // Provider ni yangilash
            if (user.getProvider() == null) {
                user.setProvider(AuthProvider.GOOGLE);
                needsUpdate = true;
            }

            if (needsUpdate) {
                user = userRepository.save(user);
                log.info("✅ User updated: {}", user.getUsername());
            }
        }

        // JWT token yaratish
        String token = jwtUtil.generateToken(user.getUsername());
        log.info("✅ JWT token generated for: {}", user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userMapper.toDto(user));
        result.put("isNewUser", isNewUser);

        return ApiResponse.success(result, "Login successful");
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase();

        if (baseUsername.length() < 3) {
            baseUsername = "user" + baseUsername;
        }

        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        log.info("Generated unique username: {}", username);
        return username;
    }

    public FirebaseUserInfoResponse getUserInfo(String idToken) {
        return firebaseAuthClient.getUserInfo(idToken);
    }
}