package com.example.tinder.service.firebase;

import com.example.tinder.dto.auth.FirebaseUserInfoResponse;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.AuthProvider;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
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
    private final OAuthUserRegistrationService oAuthUserRegistrationService;

    @Transactional
    public ApiResponse<?> loginWithGoogle(String idToken) {

        FirebaseUserInfoResponse firebaseUser = firebaseAuthClient.getUserInfo(idToken);

        String firebaseUid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String fullname = firebaseUser.getFullName();
        String picture = firebaseUser.getPicture();
        Boolean emailVerified = firebaseUser.getEmailVerified();

        if (firebaseUid == null || firebaseUid.isBlank()) {
            throw new IllegalStateException("Firebase UID not found");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Email not found");
        }

        Optional<User> optionalUser =
                userRepository.findByFirebaseUidOrEmail(firebaseUid, email);

        User user;
        boolean isNewUser = false;

        if (optionalUser.isEmpty()) {
            // 🆕 YANGI USER
            isNewUser = true;
            log.info("🆕 Creating new Firebase user: {}", email);

            String username = generateUniqueUsername(email);
            LocalDateTime createdAt = LocalDateTime.now();

            User newUser = User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email)
                    .username(username)
                    .fullname(
                            fullname != null && !fullname.isBlank()
                                    ? fullname
                                    : "User"
                    )
                    .profilePic(null)
                    .isVerified(emailVerified != null && emailVerified)
                    .provider(AuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.EVERYONE)
                    .active(true)
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .freeTonight(false)
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .birthDate(Date.valueOf("2004-05-31 05:00:00.000000"))
                    .build();
            newUser.setCreatedAt(createdAt);
            newUser.setUpdatedAt(createdAt);

            // 🔥 ENG MUHIM QATOR
            oAuthUserRegistrationService.register(newUser);

            user = newUser;

            log.info("✅ Firebase user created with all related entities");

        } else {
            // 🔁 MAVJUD USER
            user = optionalUser.get();
            boolean needsUpdate = false;

            if (user.getFirebaseUid() == null) {
                user.setFirebaseUid(firebaseUid);
                needsUpdate = true;
            }

            if (picture != null && !picture.equals(user.getProfilePic())) {
                user.setProfilePic(picture);
                needsUpdate = true;
            }

            if (fullname != null && !fullname.equals(user.getFullname())) {
                user.setFullname(fullname);
                needsUpdate = true;
            }

            if (user.getProvider() == AuthProvider.EMAIL) {
                user.setProvider(AuthProvider.EMAIL_AND_GOOGLE);
                needsUpdate = true;
            }

            if (needsUpdate) {
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(user.getUsername());

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

        log.info("📝 Generated username: {}", username);
        return username;
    }
}
