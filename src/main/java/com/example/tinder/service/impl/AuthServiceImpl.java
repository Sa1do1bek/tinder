package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.entity.*;
import com.example.tinder.enums.Role;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.mapper.PendingUserMapper;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.AuthService;
import com.example.tinder.service.DeviceService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserDetailsService userDetailsService;
    private final DeviceRepository deviceRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    private final PendingUserRepository pendingUserRepository;
    private final UserLocationRepository userLocationRepository;
    private final PendingUserMapper pendingUserMapper;

    @Override
    public ApiResponse registerByEmail(RegisterDto dto) {
        if (!utils.checkEmail(dto.getEmail())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid email format")
                    .build();
        }
        if (userRepository.existsByEmailAndActiveTrue(dto.getEmail())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Email already taken")
                    .build();
        }
        if (userRepository.existsByUsernameAndActiveTrue(dto.getUsername())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Username already taken")
                    .build();
        }
        String code = utils.getCode();
        if (!utils.sendCodeToMail(dto.getEmail(), code)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error sending verification code")
                    .build();
        }
        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByEmail(dto.getEmail());
        PendingUser pendingUser = pendingUserMapper
                .toPendingUser(optionalPendingUser.orElse(new PendingUser()), dto, code);
        pendingUser.setPassword(passwordEncoder.encode(dto.getPassword()));

        pendingUserRepository.save(pendingUser);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Verification code sent to email")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse verify(VerifyDto dto) {
        Optional<PendingUser> optionalPending = pendingUserRepository.findByEmail(dto.getEmail());

        if (optionalPending.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid verification code or email")
                    .build();
        }

        PendingUser pending = optionalPending.get();

        System.out.println("Pending code: " + pending.getCode());
        System.out.println("Dto code: " + dto.getCode());

        if (!pending.getCode().equals(dto.getCode())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid verification code or email")
                    .build();
        }


        long EXPIRATION_MINUTES = 5;
        if (pending.getCreatedAt().plusMinutes(EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            pendingUserRepository.delete(pending);
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Verification code has expired. Please register again.")
                    .build();
        }

        User user = userRepository.findByEmail(dto.getEmail()).orElseGet(User::new);

        UserRole role = roleRepository.findByName(pending.getRole()).orElse(null);
        Set<UserRole> roles = new HashSet<>();
        if (role != null) {
            roles.add(role);
        }

        boolean isNewUser = user.getId() == null;

        user.setEmail(pending.getEmail());
        user.setUsername(pending.getUsername());
        user.setPassword(pending.getPassword());
        user.setStatus(UserStatus.ACTIVE);
        user.setGender(pending.getGender());
        user.setAge(pending.getAge());
        user.setBirthDate(pending.getBirthDate());
        user.setFullname(pending.getFullName());
        user.setRole(roles);
        user.setActive(true);

        if (!isNewUser) {
            user.getProfilePictures().clear();
            user.getUserInterests().clear();
            user.setBio(null);
            user.setProfilePic(null);
            user.setUpdatedAt(LocalDateTime.now());

            userLocationRepository.findByUserId(user.getId()).ifPresent(userLocationRepository::delete);
        } else {
            user.setProfilePictures(new ArrayList<>());
            user.setUserInterests(new ArrayList<>());
            user.setBio(null);
        }

        User savedUser = userRepository.save(user);

        tokenRepository.deleteByUserId(savedUser.getId());
        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId(), pending.getFcmToken());

        String jwtToken = jwtUtil.generateToken(savedUser.getUsername());
        Token token = Token.builder()
                .user(savedUser)
                .token(jwtToken)
                .type("Bearer")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();
        tokenRepository.save(token);

        pendingUserRepository.delete(pending);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("User registered successfully")
                .data(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(savedUser)
                ))
                .build();

    }


    @Override
    @Transactional
    public ApiResponse loginByEmail(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        if (!user.isActive()) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message("Account deleted. Please register again.")
                    .build();
        }

        //delete old token
        this.tokenRepository.deleteByUserId(user.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String userToken = jwtUtil.generateToken(userDetails.getUsername());

        Token newToken = Token.builder()
                .user(user)
                .token(userToken)
                .type("Bearer")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();
        this.tokenRepository.save(newToken);

        boolean isTrustedDevice = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), dto.getDeviceId())
                .isPresent();

        if (!isTrustedDevice) {
            String code = utils.getCode();
            if (utils.sendCodeToMail(dto.getEmail(), code)) {
                Device device = Device.builder()
                        .user(user)
                        .deviceName(dto.getDeviceId())
                        .fcmToken(userToken)
                        .build();
                Device save = this.deviceRepository.save(device);

                Code c = Code.builder()
                        .user(user)
                        .device(save)
                        .code(code)
                        .createdAt(LocalDateTime.now())
                        .build();
                this.codeRepository.save(c);

                return ApiResponse.builder()
                        .code(0)
                        .status(HttpStatus.OK)
                        .message("Code sent")
                        .data(userToken)
                        .meta(Map.of("user", this.userMapper.toDto(user)))
                        .build();
            }
        }

        if (!user.isActive()) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message("Account deleted. Please register again.")
                    .build();
        }

        return ApiResponse.builder()
                .code(1)
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .data(userToken)
                .meta(Map.of("user", this.userMapper.toDto(user)))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse resendCodeToEmail(String email, String deviceId, Long id) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message("Account deleted. Please register again.")
                    .build();
        }

        Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        String code = utils.getCode();
        if (utils.sendCodeToMail(email, code)) {
            this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId())
                    .ifPresentOrElse(
                            c -> {
                                c.setCode(code);
                                c.setCreatedAt(LocalDateTime.now());
                                this.codeRepository.save(c);
                            },
                            () -> {
                                Code newCode = Code.builder()
                                        .code(code)
                                        .createdAt(LocalDateTime.now())
                                        .user(user)
                                        .device(device)
                                        .build();
                                this.codeRepository.save(newCode);
                            }
                    );

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Auth code sent successfully")
                    .build();
        }

        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Failed to send authentication code to email")
                .build();
    }

    @Override
    @Transactional
    public ApiResponse checkCodeByEmail(String email, String code, String deviceId, String fcmToken) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = this.userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User not found")
                    .build();
        }

        if (!user.isActive()) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message("Account deleted. Please register again.")
                    .build();
        }

        Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId).orElse(null);
        if (device == null) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Device record not found. Resend code or try logging in again.")
                    .build();
        }

        Code authCode = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);

        if (authCode == null || !this.utils.checkCode(email, code, device.getId())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid or expired verification code.")
                    .build();
        }

        device.setFcmToken(fcmToken);
        device.setAddedAt(LocalDateTime.now());
        device.setExpiredAt(LocalDateTime.now().plusDays(15));
        this.deviceRepository.save(device);

        authCode.setApprovedAt(LocalDateTime.now());
        this.codeRepository.save(authCode);

        String currentToken = jwtUtil.generateToken(user.getUsername());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("user", this.userMapper.toDto(user));
        responseData.put("token", currentToken);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Login SUCCESS")
                .data(responseData)
                .build();
    }

}