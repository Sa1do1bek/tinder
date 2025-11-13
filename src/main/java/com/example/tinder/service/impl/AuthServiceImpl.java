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
    private final AuthenticationManager authenticationManager;
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

        Optional<User> optionalUser = userRepository.findByEmail(pending.getEmail());
        UserRole role = roleRepository.findByName(pending.getRole()).orElse(null);

        User user;
        Set<UserRole> roles = new HashSet<>();
        if (role != null) {
            roles.add(role);
        }

        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            user.setUsername(pending.getUsername());
            user.setPassword(pending.getPassword());
            user.setStatus(UserStatus.ACTIVE);
            user.setGender(pending.getGender());
            user.setBirthDate(pending.getBirthDate());
            user.setFullname(pending.getFullName());

            user.getProfilePictures().clear();
            user.getUserInterests().clear();
            user.setBio(null);
            user.setProfilePic(null);

            userLocationRepository.findByUserId(user.getId()).ifPresent(userLocationRepository::delete);

            user.setRole(roles);
            user.setActive(true);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user = User.builder()
                    .email(pending.getEmail())
                    .username(pending.getUsername())
                    .password(pending.getPassword())
                    .status(UserStatus.ACTIVE)
                    .gender(pending.getGender())
                    .birthDate(pending.getBirthDate())
                    .fullname(pending.getFullName())
                    .role(roles)
                    .active(true)
                    .profilePictures(new ArrayList<>())
                    .userInterests(new ArrayList<>())
                    .bio(null)
                    .build();
        }

        User savedUser = userRepository.save(user);

        tokenRepository.deleteByUserId(savedUser.getId());

        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId());

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

        User user;
        try {
            user = this.userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (ResourceNotFoundException e) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid authentication characters")
                    .build();
        }

        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getPassword());
            authenticationManager.authenticate(authentication);
        } catch (Exception e) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid credentials")
                    .build();
        }

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

        Device trustedDevice = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), dto.getDeviceId())
                .orElse(null);

        String code = utils.getCode();
        if (trustedDevice == null) {
            if (utils.sendCodeToMail(dto.getEmail(), code)) {
                Device device = new Device();
                device.setUser(user);
                device.setDeviceName(dto.getDeviceId());
                Device save = this.deviceRepository.save(device);

                Code c = new Code();
                c.setCreatedAt(LocalDateTime.now());
                c.setCode(code);
                c.setUser(user);
                c.setDevice(save);
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
            Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);
            if (c != null) {
                c.setCode(code);
                c.setCreatedAt(LocalDateTime.now());
                this.codeRepository.save(c);
            }
            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Auth code sent")
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Error while sending auth code")
                .build();
    }

    @Override
    public ApiResponse checkCodeByEmail(String email, String code, String deviceId) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = this.userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (!user.isActive()) {
                return ApiResponse.builder()
                        .status(HttpStatus.FORBIDDEN)
                        .message("Account deleted. Please register again.")
                        .build();
            }
            Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                    .orElse(null);
            if (device != null) {
                Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);

                if (c != null && c.getDevice() != null && c.getDevice().getUser() != null
                        && c.getDevice().getUser().getId().equals(user.getId())) {
                    c.getDevice().setAddedAt(LocalDateTime.now());
                    c.getDevice().setDeviceName(deviceId);
                    c.getDevice().setExpiredAt(LocalDateTime.now().plusDays(15));
                    this.deviceRepository.save(c.getDevice());
                }

                if (!this.utils.checkCode(email, code, device.getId())) {
                    return ApiResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error")
                            .build();
                }

                Map<String, Object> map = new HashMap<>();
                Device devices = this.deviceService.addTrustedDeviceByLogin(user.getId(), deviceId);
                devices.setUser(user);
                devices.setDeviceName(deviceId);
                Device save = this.deviceRepository.save(devices);

                Code cc = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);
                if (cc != null) {
                    cc.setApprovedAt(LocalDateTime.now());
                    cc.setDevice(save);
                    this.codeRepository.save(cc);
                }


                String currentToken = jwtUtil.generateToken(user.getUsername());
                map.put("user", this.userMapper.toDto(user));

                return ApiResponse.builder()
                        .status(HttpStatus.OK)
                        .message("SUCCESS")
                        .data(map)
                        .build();
            }
        }

        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("User not found")
                .build();
    }


}